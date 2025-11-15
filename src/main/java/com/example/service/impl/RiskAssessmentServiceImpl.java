package com.example.service.impl;

import com.example.model.Account;
import com.example.model.Customer;
import com.example.model.RiskAssessment;
import com.example.model.Transaction;
import com.example.model.dto.RiskAssessmentRequest;
import com.example.model.dto.RiskAssessmentResponse;
import com.example.repository.AccountRepository;
import com.example.repository.CustomerRepository;
import com.example.repository.RiskAssessmentRepository;
import com.example.repository.TransactionRepository;
import com.example.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentServiceImpl implements RiskAssessmentService {
    
    private final ChatClient chatClient;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    
    @Override
    @Transactional
    public RiskAssessmentResponse assessRisk(RiskAssessmentRequest request) {
        log.info("Assessing risk for account: {}, customer: {}", 
                request.getAccountNumber(), request.getCustomerId());
        
        var account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        var customer = customerRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        // Gather comprehensive data for risk assessment
        List<Transaction> transactionHistory = request.getIncludeTransactionHistory() != null && 
                request.getIncludeTransactionHistory()
                ? transactionRepository.findRecentTransactionsByAccount(
                        request.getAccountNumber(), LocalDateTime.now().minusMonths(6))
                : Collections.emptyList();
        
        // Build risk assessment context
        String riskContext = buildRiskContext(account, customer, transactionHistory);
        
        // Use AI to perform comprehensive risk assessment
        String riskAssessmentPrompt = """
                You are a senior risk analyst for a banking institution.
                Perform a comprehensive risk assessment based on the following information.
                
                {riskContext}
                
                Analyze the following risk factors:
                1. Transaction patterns and anomalies
                2. Account activity and behavior
                3. Customer profile and history
                4. Compliance and regulatory considerations
                5. Financial stability indicators
                
                Provide your assessment in the following format:
                OVERALL_RISK_LEVEL: [LOW/MEDIUM/HIGH/CRITICAL]
                OVERALL_RISK_SCORE: [0.0 to 1.0]
                ANALYSIS: [Detailed risk analysis]
                RISK_FACTORS: [Comma-separated list of identified risk factors]
                RECOMMENDATIONS: [Comma-separated list of risk mitigation recommendations]
                """;
        
        PromptTemplate promptTemplate = new PromptTemplate(riskAssessmentPrompt);
        Map<String, Object> variables = new HashMap<>();
        variables.put("riskContext", riskContext);
        
        Prompt prompt = promptTemplate.create(variables);
        String aiResponse = chatClient.prompt(prompt).call().content();
        
        // Parse AI response
        RiskAssessmentResult result = parseRiskAssessment(aiResponse);
        
        // Save risk assessment
        RiskAssessment riskAssessment = RiskAssessment.builder()
                .accountNumber(request.getAccountNumber())
                .customerId(request.getCustomerId())
                .overallRiskLevel(result.riskLevel)
                .overallRiskScore(result.riskScore)
                .aiAnalysis(result.analysis)
                .riskFactors(result.riskFactors.toString())
                .recommendations(result.recommendations.toString())
                .assessmentDate(LocalDateTime.now())
                .build();
        
        riskAssessmentRepository.save(riskAssessment);
        
        return RiskAssessmentResponse.builder()
                .accountNumber(request.getAccountNumber())
                .customerId(request.getCustomerId())
                .overallRiskLevel(result.riskLevel)
                .overallRiskScore(result.riskScore)
                .aiAnalysis(result.analysis)
                .riskFactors(result.riskFactors)
                .recommendations(result.recommendations)
                .build();
    }
    
    private String buildRiskContext(Account account, Customer customer, List<Transaction> transactions) {
        StringBuilder context = new StringBuilder();
        
        context.append(String.format("""
                Customer Profile:
                - Customer ID: %s
                - Name: %s %s
                - KYC Status: %s
                - Current Risk Profile: %s
                - Account Age: Active since %s
                
                Account Information:
                - Account Number: %s
                - Account Type: %s
                - Balance: %s %s
                - Status: %s
                - Credit Limit: %s
                
                """, 
                customer.getCustomerId(),
                customer.getFirstName(), customer.getLastName(),
                customer.getKycStatus(), customer.getRiskProfile(),
                account.getOpenedDate(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(), account.getCurrency(),
                account.getStatus(),
                account.getCreditLimit() != null ? account.getCreditLimit() : "N/A"));
        
        if (!transactions.isEmpty()) {
            context.append("Transaction History (Last 6 months):\n");
            
            // Calculate transaction statistics
            long totalTransactions = transactions.size();
            long debitCount = transactions.stream()
                    .filter(t -> "DEBIT".equals(t.getTransactionType()))
                    .count();
            long creditCount = transactions.stream()
                    .filter(t -> "CREDIT".equals(t.getTransactionType()))
                    .count();
            
            // Category distribution
            Map<String, Long> categoryCount = transactions.stream()
                    .collect(Collectors.groupingBy(Transaction::getMerchantCategory, Collectors.counting()));
            
            context.append(String.format("""
                    - Total Transactions: %d
                    - Debits: %d
                    - Credits: %d
                    - Category Distribution: %s
                    
                    """, totalTransactions, debitCount, creditCount, categoryCount));
            
            // Recent unusual transactions
            List<Transaction> unusualTransactions = transactions.stream()
                    .filter(t -> "FRAUD_SUSPECTED".equals(t.getStatus()) || 
                                "PENDING".equals(t.getStatus()))
                    .limit(5)
                    .toList();
            
            if (!unusualTransactions.isEmpty()) {
                context.append("Unusual/Flagged Transactions:\n");
                unusualTransactions.forEach(t -> 
                        context.append(String.format("- %s: %s %s at %s (Status: %s)\n",
                                t.getTransactionType(), t.getAmount(), t.getCurrency(),
                                t.getMerchantName(), t.getStatus())));
            }
        } else {
            context.append("No transaction history available for analysis.\n");
        }
        
        return context.toString();
    }
    
    private RiskAssessmentResult parseRiskAssessment(String aiResponse) {
        String riskLevel = "MEDIUM";
        double riskScore = 0.5;
        String analysis = aiResponse;
        List<String> riskFactors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        try {
            String[] lines = aiResponse.split("\n");
            for (String line : lines) {
                if (line.startsWith("OVERALL_RISK_LEVEL:")) {
                    riskLevel = line.substring("OVERALL_RISK_LEVEL:".length()).trim();
                } else if (line.startsWith("OVERALL_RISK_SCORE:")) {
                    String scoreStr = line.substring("OVERALL_RISK_SCORE:".length()).trim();
                    riskScore = Double.parseDouble(scoreStr);
                } else if (line.startsWith("RISK_FACTORS:")) {
                    String factors = line.substring("RISK_FACTORS:".length()).trim();
                    riskFactors = Arrays.asList(factors.split(","));
                } else if (line.startsWith("RECOMMENDATIONS:")) {
                    String recs = line.substring("RECOMMENDATIONS:".length()).trim();
                    recommendations = Arrays.asList(recs.split(","));
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing risk assessment response, using defaults", e);
            riskFactors = List.of("AI analysis completed. Review detailed analysis.");
            recommendations = List.of("Continue monitoring account activity.");
        }
        
        return new RiskAssessmentResult(riskLevel, riskScore, analysis, riskFactors, recommendations);
    }
    
    private record RiskAssessmentResult(
            String riskLevel,
            double riskScore,
            String analysis,
            List<String> riskFactors,
            List<String> recommendations
    ) {}
}

