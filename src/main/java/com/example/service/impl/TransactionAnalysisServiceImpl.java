package com.example.service.impl;

import com.example.model.Transaction;
import com.example.model.dto.TransactionAnalysisRequest;
import com.example.model.dto.TransactionAnalysisResponse;
import com.example.repository.AccountRepository;
import com.example.repository.TransactionRepository;
import com.example.service.TransactionAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAnalysisServiceImpl implements TransactionAnalysisService {
    
    private final ChatClient chatClient;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    
    @Override
    public TransactionAnalysisResponse analyzeTransactions(TransactionAnalysisRequest request) {
        log.info("Analyzing transactions for account: {}", request.getAccountNumber());
        
        var account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountNumber()));
        
        LocalDateTime startDate = request.getStartDate() != null 
                ? request.getStartDate() 
                : LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = request.getEndDate() != null 
                ? request.getEndDate() 
                : LocalDateTime.now();
        
        List<Transaction> transactions = transactionRepository
                .findByAccountNumberAndTransactionDateBetween(
                        request.getAccountNumber(), startDate, endDate);
        
        if (transactions.isEmpty()) {
            return TransactionAnalysisResponse.builder()
                    .accountNumber(request.getAccountNumber())
                    .analysisType(request.getAnalysisType() != null ? request.getAnalysisType() : "SPENDING_PATTERNS")
                    .aiInsights("No transactions found for the specified period.")
                    .categoryBreakdown(Map.of())
                    .keyFindings(List.of())
                    .recommendations(List.of())
                    .statistics(Map.of())
                    .build();
        }
        
        // Calculate statistics
        Map<String, Object> statistics = calculateStatistics(transactions);
        Map<String, BigDecimal> categoryBreakdown = calculateCategoryBreakdown(transactions);
        
        // Generate AI insights
        String analysisType = request.getAnalysisType() != null 
                ? request.getAnalysisType() 
                : "SPENDING_PATTERNS";
        
        String aiInsights = generateAIInsights(transactions, account, statistics, categoryBreakdown, analysisType);
        
        // Extract key findings and recommendations from AI response
        List<String> keyFindings = extractKeyFindings(aiInsights);
        List<String> recommendations = extractRecommendations(aiInsights);
        
        return TransactionAnalysisResponse.builder()
                .accountNumber(request.getAccountNumber())
                .analysisType(analysisType)
                .aiInsights(aiInsights)
                .categoryBreakdown(categoryBreakdown)
                .keyFindings(keyFindings)
                .recommendations(recommendations)
                .statistics(statistics)
                .build();
    }
    
    private Map<String, Object> calculateStatistics(List<Transaction> transactions) {
        BigDecimal totalDebits = transactions.stream()
                .filter(t -> "DEBIT".equals(t.getTransactionType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredits = transactions.stream()
                .filter(t -> "CREDIT".equals(t.getTransactionType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netAmount = totalCredits.subtract(totalDebits);
        
        long transactionCount = transactions.size();
        BigDecimal averageTransaction = transactionCount > 0 
                ? totalDebits.divide(BigDecimal.valueOf(transactionCount), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", transactionCount);
        stats.put("totalDebits", totalDebits);
        stats.put("totalCredits", totalCredits);
        stats.put("netAmount", netAmount);
        stats.put("averageTransactionAmount", averageTransaction);
        stats.put("periodStart", transactions.stream()
                .map(Transaction::getTransactionDate)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now()));
        stats.put("periodEnd", transactions.stream()
                .map(Transaction::getTransactionDate)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now()));
        
        return stats;
    }
    
    private Map<String, BigDecimal> calculateCategoryBreakdown(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> "DEBIT".equals(t.getTransactionType()))
                .collect(Collectors.groupingBy(
                        Transaction::getMerchantCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }
    
    private String generateAIInsights(List<Transaction> transactions, 
                                     com.example.model.Account account,
                                     Map<String, Object> statistics,
                                     Map<String, BigDecimal> categoryBreakdown,
                                     String analysisType) {
        String promptTemplate = """
                You are a financial analyst for a banking institution. Analyze the following transaction data and provide insights.
                
                Account Information:
                - Account Number: {accountNumber}
                - Account Type: {accountType}
                - Current Balance: {balance}
                
                Transaction Statistics:
                {statistics}
                
                Category Breakdown:
                {categoryBreakdown}
                
                Analysis Type Requested: {analysisType}
                
                Please provide:
                1. Key insights about spending patterns, trends, or anomalies
                2. Notable findings (e.g., unusual spending, category trends, timing patterns)
                3. Actionable recommendations for the customer
                
                Format your response clearly with sections for INSIGHTS, FINDINGS, and RECOMMENDATIONS.
                """;
        
        PromptTemplate template = new PromptTemplate(promptTemplate);
        Map<String, Object> variables = new HashMap<>();
        variables.put("accountNumber", account.getAccountNumber());
        variables.put("accountType", account.getAccountType());
        variables.put("balance", account.getBalance());
        variables.put("statistics", formatStatistics(statistics));
        variables.put("categoryBreakdown", formatCategoryBreakdown(categoryBreakdown));
        variables.put("analysisType", analysisType);
        
        Prompt prompt = template.create(variables);
        return chatClient.prompt(prompt).call().content();
    }
    
    private String formatStatistics(Map<String, Object> statistics) {
        StringBuilder sb = new StringBuilder();
        statistics.forEach((key, value) -> sb.append(String.format("- %s: %s\n", key, value)));
        return sb.toString();
    }
    
    private String formatCategoryBreakdown(Map<String, BigDecimal> categoryBreakdown) {
        if (categoryBreakdown.isEmpty()) {
            return "No category data available";
        }
        StringBuilder sb = new StringBuilder();
        categoryBreakdown.forEach((category, amount) -> 
                sb.append(String.format("- %s: %s\n", category, amount)));
        return sb.toString();
    }
    
    private List<String> extractKeyFindings(String aiResponse) {
        List<String> findings = new ArrayList<>();
        String[] lines = aiResponse.split("\n");
        boolean inFindingsSection = false;
        
        for (String line : lines) {
            if (line.toUpperCase().contains("FINDINGS") || line.toUpperCase().contains("KEY FINDINGS")) {
                inFindingsSection = true;
                continue;
            }
            if (inFindingsSection && (line.trim().startsWith("-") || line.trim().matches("\\d+\\."))) {
                findings.add(line.trim().replaceFirst("^[-\\d.]\\s*", ""));
            }
            if (inFindingsSection && (line.toUpperCase().contains("RECOMMENDATIONS") || 
                                     line.toUpperCase().contains("INSIGHTS"))) {
                break;
            }
        }
        
        return findings.isEmpty() ? List.of("Analysis completed. Review AI insights for details.") : findings;
    }
    
    private List<String> extractRecommendations(String aiResponse) {
        List<String> recommendations = new ArrayList<>();
        String[] lines = aiResponse.split("\n");
        boolean inRecommendationsSection = false;
        
        for (String line : lines) {
            if (line.toUpperCase().contains("RECOMMENDATIONS")) {
                inRecommendationsSection = true;
                continue;
            }
            if (inRecommendationsSection && (line.trim().startsWith("-") || line.trim().matches("\\d+\\."))) {
                recommendations.add(line.trim().replaceFirst("^[-\\d.]\\s*", ""));
            }
        }
        
        return recommendations.isEmpty() ? List.of("Continue monitoring account activity.") : recommendations;
    }
}

