package com.example.service.impl;

import com.example.model.Account;
import com.example.model.ComplianceReport;
import com.example.model.Customer;
import com.example.model.Transaction;
import com.example.model.dto.ComplianceCheckRequest;
import com.example.model.dto.ComplianceCheckResponse;
import com.example.repository.AccountRepository;
import com.example.repository.ComplianceReportRepository;
import com.example.repository.CustomerRepository;
import com.example.repository.TransactionRepository;
import com.example.service.ComplianceService;
import com.example.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceServiceImpl implements ComplianceService {
    
    private final ChatClient chatClient;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final ComplianceReportRepository complianceReportRepository;
    private final RAGService ragService;
    
    @Override
    @Transactional
    public ComplianceCheckResponse checkCompliance(ComplianceCheckRequest request) {
        log.info("Performing compliance check: type={}, account={}, customer={}", 
                request.getComplianceType(), request.getAccountNumber(), request.getCustomerId());
        
        var account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        var customer = customerRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        // Gather relevant data for compliance check
        List<Transaction> recentTransactions = transactionRepository
                .findRecentTransactionsByAccount(
                        request.getAccountNumber(), 
                        LocalDateTime.now().minusMonths(12));
        
        // Build compliance context
        String complianceContext = buildComplianceContext(account, customer, recentTransactions, request.getComplianceType());
        
        // Retrieve relevant compliance knowledge using RAG
        String ragContext = ragService.retrieveRelevantContext(
                String.format("%s compliance regulations requirements", request.getComplianceType()), 
                3);
        
        // Generate compliance-specific prompt
        String compliancePrompt = getCompliancePrompt(request.getComplianceType());
        
        PromptTemplate promptTemplate = new PromptTemplate(compliancePrompt);
        Map<String, Object> variables = new HashMap<>();
        variables.put("ragContext", ragContext);
        variables.put("complianceContext", complianceContext);
        variables.put("complianceType", request.getComplianceType());
        
        Prompt prompt = promptTemplate.create(variables);
        String aiResponse = chatClient.prompt(prompt).call().content();
        
        // Parse AI response
        ComplianceResult result = parseComplianceResult(aiResponse);
        
        // Save compliance report
        ComplianceReport report = ComplianceReport.builder()
                .accountNumber(request.getAccountNumber())
                .customerId(request.getCustomerId())
                .complianceType(request.getComplianceType())
                .status(result.status)
                .aiAnalysis(result.analysis)
                .findings(result.findings.toString())
                .recommendations(result.recommendations.toString())
                .reportDate(LocalDateTime.now())
                .build();
        
        complianceReportRepository.save(report);
        
        return ComplianceCheckResponse.builder()
                .accountNumber(request.getAccountNumber())
                .customerId(request.getCustomerId())
                .complianceType(request.getComplianceType())
                .status(result.status)
                .aiAnalysis(result.analysis)
                .findings(result.findings)
                .recommendations(result.recommendations)
                .build();
    }
    
    private String buildComplianceContext(Account account, Customer customer, 
                                         List<Transaction> transactions, 
                                         String complianceType) {
        StringBuilder context = new StringBuilder();
        
        context.append(String.format("""
                Customer Information:
                - Customer ID: %s
                - Name: %s %s
                - Date of Birth: %s
                - Address: %s, %s, %s
                - KYC Status: %s
                - Risk Profile: %s
                
                Account Information:
                - Account Number: %s
                - Account Type: %s
                - Balance: %s %s
                - Opened Date: %s
                - Status: %s
                
                """,
                customer.getCustomerId(),
                customer.getFirstName(), customer.getLastName(),
                customer.getDateOfBirth(),
                customer.getAddress(), customer.getCity(), customer.getCountry(),
                customer.getKycStatus(), customer.getRiskProfile(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(), account.getCurrency(),
                account.getOpenedDate(),
                account.getStatus()));
        
        if (!transactions.isEmpty()) {
            context.append(String.format("Transaction History (Last 12 months): %d transactions\n", transactions.size()));
            
            // Identify high-value transactions
            transactions.stream()
                    .filter(t -> t.getAmount().doubleValue() > 10000)
                    .limit(10)
                    .forEach(t -> context.append(String.format(
                            "- High-value: %s %s %s at %s on %s\n",
                            t.getTransactionType(), t.getAmount(), t.getCurrency(),
                            t.getMerchantName(), t.getTransactionDate())));
            
            // Identify international transactions
            transactions.stream()
                    .filter(t -> t.getLocation() != null && 
                            (t.getLocation().toLowerCase().contains("offshore") ||
                             !t.getLocation().toLowerCase().contains("usa")))
                    .limit(10)
                    .forEach(t -> context.append(String.format(
                            "- International: %s %s at %s (%s) on %s\n",
                            t.getAmount(), t.getCurrency(),
                            t.getMerchantName(), t.getLocation(), t.getTransactionDate())));
        }
        
        return context.toString();
    }
    
    private String getCompliancePrompt(String complianceType) {
        return switch (complianceType.toUpperCase()) {
            case "AML" -> """
                    You are an Anti-Money Laundering (AML) compliance expert.
                    Analyze the following customer and account information for AML compliance.
                    
                    Relevant AML Regulations and Guidelines:
                    {ragContext}
                    
                    {complianceContext}
                    
                    Check for:
                    1. Suspicious transaction patterns
                    2. Unusual account activity
                    3. High-risk transactions
                    4. Structuring or smurfing patterns
                    5. Unusual geographic patterns
                    
                    Provide assessment in format:
                    STATUS: [COMPLIANT/NON_COMPLIANT/REQUIRES_REVIEW]
                    ANALYSIS: [Detailed compliance analysis]
                    FINDINGS: [Comma-separated list of findings]
                    RECOMMENDATIONS: [Comma-separated list of recommendations]
                    """;
            case "KYC" -> """
                    You are a Know Your Customer (KYC) compliance expert.
                    Analyze the following customer information for KYC compliance.
                    
                    Relevant KYC Regulations and Guidelines:
                    {ragContext}
                    
                    {complianceContext}
                    
                    Check for:
                    1. Customer identification and verification
                    2. Customer due diligence requirements
                    3. Beneficial ownership information
                    4. Ongoing monitoring requirements
                    5. Risk-based approach compliance
                    
                    Provide assessment in format:
                    STATUS: [COMPLIANT/NON_COMPLIANT/REQUIRES_REVIEW]
                    ANALYSIS: [Detailed compliance analysis]
                    FINDINGS: [Comma-separated list of findings]
                    RECOMMENDATIONS: [Comma-separated list of recommendations]
                    """;
            case "SANCTIONS" -> """
                    You are a Sanctions screening compliance expert.
                    Analyze the following information for sanctions compliance.
                    
                    {complianceContext}
                    
                    Check for:
                    1. Sanctions list matches
                    2. PEP (Politically Exposed Person) status
                    3. High-risk jurisdictions
                    4. Sanctioned entities or individuals
                    5. Transaction screening requirements
                    
                    Provide assessment in format:
                    STATUS: [COMPLIANT/NON_COMPLIANT/REQUIRES_REVIEW]
                    ANALYSIS: [Detailed compliance analysis]
                    FINDINGS: [Comma-separated list of findings]
                    RECOMMENDATIONS: [Comma-separated list of recommendations]
                    """;
            default -> """
                    You are a regulatory compliance expert.
                    Analyze the following information for {complianceType} compliance.
                    
                    {complianceContext}
                    
                    Provide assessment in format:
                    STATUS: [COMPLIANT/NON_COMPLIANT/REQUIRES_REVIEW]
                    ANALYSIS: [Detailed compliance analysis]
                    FINDINGS: [Comma-separated list of findings]
                    RECOMMENDATIONS: [Comma-separated list of recommendations]
                    """;
        };
    }
    
    private ComplianceResult parseComplianceResult(String aiResponse) {
        String status = "REQUIRES_REVIEW";
        String analysis = aiResponse;
        List<String> findings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        try {
            String[] lines = aiResponse.split("\n");
            for (String line : lines) {
                if (line.startsWith("STATUS:")) {
                    status = line.substring("STATUS:".length()).trim();
                } else if (line.startsWith("FINDINGS:")) {
                    String finds = line.substring("FINDINGS:".length()).trim();
                    findings = Arrays.asList(finds.split(","));
                } else if (line.startsWith("RECOMMENDATIONS:")) {
                    String recs = line.substring("RECOMMENDATIONS:".length()).trim();
                    recommendations = Arrays.asList(recs.split(","));
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing compliance result, using defaults", e);
            findings = List.of("Compliance check completed. Review detailed analysis.");
            recommendations = List.of("Continue monitoring for compliance.");
        }
        
        return new ComplianceResult(status, analysis, findings, recommendations);
    }
    
    private record ComplianceResult(
            String status,
            String analysis,
            List<String> findings,
            List<String> recommendations
    ) {}
}

