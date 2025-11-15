package com.example.service;

import com.example.model.dto.FraudAnalysisResult;
import com.example.model.dto.RiskAssessmentResult;
import com.example.model.dto.TransactionSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Structured Output Service
 * Demonstrates Spring AI's ability to return structured JSON responses
 * mapped to POJOs for type-safe handling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StructuredOutputService {

    private final ChatClient chatClient;

    /**
     * Get structured fraud analysis result
     * AI returns a structured FraudAnalysisResult object
     */
    public FraudAnalysisResult analyzeFraudStructured(String transactionDetails) {
        log.info("Analyzing fraud with structured output: {}", transactionDetails);
        
        String prompt = String.format("""
                Analyze the following transaction for fraud and return a structured response:
                
                Transaction: %s
                
                Return a JSON object with:
                - riskScore: number between 0-100
                - isFraudulent: boolean
                - riskLevel: one of LOW, MEDIUM, HIGH, CRITICAL
                - reasons: array of strings explaining the risk factors
                - recommendation: string with recommended action
                """, transactionDetails);

        // Spring AI can map responses to POJOs
        // Note: This requires the model to support structured outputs
        // For Ollama, we may need to use prompt engineering to get JSON
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // Parse JSON response to FraudAnalysisResult
        // In production, use Jackson or similar to parse
        return parseFraudAnalysis(response);
    }

    /**
     * Get structured transaction summary
     */
    public TransactionSummary getTransactionSummaryStructured(String accountNumber, int days) {
        log.info("Getting structured transaction summary for account: {}, days: {}", accountNumber, days);
        
        String prompt = String.format("""
                Analyze transactions for account %s over the last %d days.
                Return a JSON object with:
                - totalTransactions: number
                - totalAmount: number
                - averageTransaction: number
                - topCategories: array of strings (top 3 categories)
                - spendingTrend: one of INCREASING, DECREASING, STABLE
                - insights: array of strings with key insights
                """, accountNumber, days);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseTransactionSummary(response);
    }

    /**
     * Get structured risk assessment
     */
    public RiskAssessmentResult assessRiskStructured(String customerId, String transactionDetails) {
        log.info("Assessing risk with structured output for customer: {}", customerId);
        
        String prompt = String.format("""
                Assess the risk for customer %s with transaction: %s
                
                Return a JSON object with:
                - overallRiskScore: number 0-100
                - riskCategory: one of LOW, MEDIUM, HIGH
                - factors: array of risk factors with name and score
                - recommendations: array of strings
                - requiresReview: boolean
                """, customerId, transactionDetails);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseRiskAssessment(response);
    }

    // Helper methods to parse JSON responses
    // In production, use proper JSON parsing with Jackson
    private FraudAnalysisResult parseFraudAnalysis(String jsonResponse) {
        // Simplified parsing - in production use Jackson ObjectMapper
        // This is a placeholder showing the concept
        return FraudAnalysisResult.builder()
                .riskScore(75)
                .isFraudulent(false)
                .riskLevel("MEDIUM")
                .reasons(List.of("Unusual transaction pattern", "High amount"))
                .recommendation("Review transaction and contact customer")
                .build();
    }

    private TransactionSummary parseTransactionSummary(String jsonResponse) {
        return TransactionSummary.builder()
                .totalTransactions(50)
                .totalAmount(new BigDecimal("5000.00"))
                .averageTransaction(new BigDecimal("100.00"))
                .topCategories(List.of("GROCERIES", "RESTAURANTS", "UTILITIES"))
                .spendingTrend("STABLE")
                .insights(List.of("Consistent spending pattern", "No unusual activity"))
                .build();
    }

    private RiskAssessmentResult parseRiskAssessment(String jsonResponse) {
        return RiskAssessmentResult.builder()
                .overallRiskScore(45)
                .riskCategory("MEDIUM")
                .factors(List.of())
                .recommendations(List.of("Monitor account activity", "Regular review recommended"))
                .requiresReview(false)
                .build();
    }
}

