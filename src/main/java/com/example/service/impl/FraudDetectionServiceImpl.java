package com.example.service.impl;

import com.example.model.FraudAlert;
import com.example.model.Transaction;
import com.example.model.dto.FraudDetectionRequest;
import com.example.model.dto.FraudDetectionResponse;
import com.example.repository.AccountRepository;
import com.example.repository.FraudAlertRepository;
import com.example.repository.TransactionRepository;
import com.example.service.FraudDetectionService;
import com.example.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionServiceImpl implements FraudDetectionService {
    
    private final ChatClient chatClient;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final RAGService ragService;
    
    @Override
    @Transactional
    public FraudDetectionResponse detectFraud(FraudDetectionRequest request) {
        log.info("Analyzing transaction for fraud: account={}, amount={}", 
                request.getAccountNumber(), request.getAmount());
        
        // Get account and recent transaction history
        var account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountNumber()));
        
        List<Transaction> recentTransactions = transactionRepository
                .findRecentTransactionsByAccount(request.getAccountNumber(), 
                        LocalDateTime.now().minusDays(30));
        
        // Build context for AI analysis
        String transactionContext = buildTransactionContext(request, account, recentTransactions);
        
        // Retrieve relevant banking knowledge using RAG
        String ragContext = ragService.retrieveRelevantContext(
                String.format("fraud detection transaction analysis %s %s %s", 
                        request.getTransactionType(), 
                        request.getAmount(), 
                        request.getMerchantCategory()), 
                3);

        // Use AI to analyze fraud patterns with RAG context
        String fraudAnalysisPrompt = """
                You are an expert fraud detection analyst for a banking institution.
                Analyze the following transaction and determine if it's potentially fraudulent.
                
                {ragContext}
                
                Transaction Details:
                {transactionDetails}
                
                Account Information:
                - Account Number: {accountNumber}
                - Account Type: {accountType}
                - Current Balance: {balance}
                - Account Status: {status}
                
                Recent Transaction History (Last 30 days):
                {recentTransactions}
                
                Please provide:
                1. Risk Score (0.0 to 1.0, where 1.0 is highest risk)
                2. Severity Level (LOW, MEDIUM, HIGH, CRITICAL)
                3. Detailed analysis of potential fraud indicators
                4. List of specific risk factors identified
                5. Recommendation (APPROVE, REVIEW, BLOCK)
                
                Format your response as:
                RISK_SCORE: [score]
                SEVERITY: [level]
                ANALYSIS: [detailed analysis]
                RISK_FACTORS: [comma-separated list]
                RECOMMENDATION: [recommendation]
                """;
        
        PromptTemplate promptTemplate = new PromptTemplate(fraudAnalysisPrompt);
        Map<String, Object> variables = new HashMap<>();
        variables.put("ragContext", ragContext);
        variables.put("transactionDetails", transactionContext);
        variables.put("accountNumber", account.getAccountNumber());
        variables.put("accountType", account.getAccountType());
        variables.put("balance", account.getBalance());
        variables.put("status", account.getStatus());
        variables.put("recentTransactions", formatTransactionHistory(recentTransactions));
        
        Prompt prompt = promptTemplate.create(variables);
        String aiResponse = chatClient.prompt(prompt).call().content();
        
        // Parse AI response
        FraudAnalysisResult analysisResult = parseFraudAnalysis(aiResponse);
        
        // Save transaction
        Transaction transaction = Transaction.builder()
                .accountNumber(request.getAccountNumber())
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantName(request.getMerchantName())
                .merchantCategory(request.getMerchantCategory())
                .location(request.getLocation())
                .transactionDate(request.getTransactionDate())
                .status(analysisResult.recommendation.equals("BLOCK") ? "FRAUD_SUSPECTED" : "PENDING")
                .description(request.getDescription())
                .counterpartyAccount(request.getCounterpartyAccount())
                .build();
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Create fraud alert if risk is medium or higher
        if (analysisResult.riskScore >= 0.5) {
            FraudAlert fraudAlert = FraudAlert.builder()
                    .transactionId(savedTransaction.getId())
                    .accountNumber(request.getAccountNumber())
                    .severity(analysisResult.severity)
                    .aiAnalysis(analysisResult.analysis)
                    .riskFactors(String.join(", ", analysisResult.riskFactors))
                    .riskScore(analysisResult.riskScore)
                    .status("PENDING")
                    .build();
            
            fraudAlertRepository.save(fraudAlert);
        }
        
        return FraudDetectionResponse.builder()
                .transactionId(savedTransaction.getId())
                .accountNumber(request.getAccountNumber())
                .severity(analysisResult.severity)
                .aiAnalysis(analysisResult.analysis)
                .riskFactors(analysisResult.riskFactors.toString())
                .riskScore(analysisResult.riskScore)
                .recommendation(analysisResult.recommendation)
                .isFraudulent(analysisResult.riskScore >= 0.7)
                .build();
    }
    
    private String buildTransactionContext(FraudDetectionRequest request, 
                                          com.example.model.Account account, 
                                          List<Transaction> recentTransactions) {
        return String.format("""
                Type: %s
                Amount: %s %s
                Merchant: %s (%s)
                Location: %s
                Date: %s
                Description: %s
                Counterparty: %s
                """, 
                request.getTransactionType(),
                request.getAmount(), request.getCurrency(),
                request.getMerchantName(), request.getMerchantCategory(),
                request.getLocation(),
                request.getTransactionDate(),
                request.getDescription() != null ? request.getDescription() : "N/A",
                request.getCounterpartyAccount() != null ? request.getCounterpartyAccount() : "N/A");
    }
    
    private String formatTransactionHistory(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "No recent transactions";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Transaction t : transactions) {
            sb.append(String.format("- %s: %s %s at %s (%s) on %s\n",
                    t.getTransactionType(),
                    t.getAmount(), t.getCurrency(),
                    t.getMerchantName(), t.getMerchantCategory(),
                    t.getTransactionDate()));
        }
        return sb.toString();
    }
    
    private FraudAnalysisResult parseFraudAnalysis(String aiResponse) {
        // Parse the structured AI response
        double riskScore = 0.5;
        String severity = "MEDIUM";
        String analysis = aiResponse;
        List<String> riskFactors = List.of("AI analysis pending");
        String recommendation = "REVIEW";
        
        try {
            String[] lines = aiResponse.split("\n");
            for (String line : lines) {
                if (line.startsWith("RISK_SCORE:")) {
                    String scoreStr = line.substring("RISK_SCORE:".length()).trim();
                    riskScore = Double.parseDouble(scoreStr);
                } else if (line.startsWith("SEVERITY:")) {
                    severity = line.substring("SEVERITY:".length()).trim();
                } else if (line.startsWith("RISK_FACTORS:")) {
                    String factors = line.substring("RISK_FACTORS:".length()).trim();
                    riskFactors = List.of(factors.split(","));
                } else if (line.startsWith("RECOMMENDATION:")) {
                    recommendation = line.substring("RECOMMENDATION:".length()).trim();
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing AI response, using defaults", e);
        }
        
        return new FraudAnalysisResult(riskScore, severity, analysis, riskFactors, recommendation);
    }
    
    private record FraudAnalysisResult(
            double riskScore,
            String severity,
            String analysis,
            List<String> riskFactors,
            String recommendation
    ) {}
}

