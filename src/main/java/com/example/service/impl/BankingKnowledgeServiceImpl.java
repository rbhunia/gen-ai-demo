package com.example.service.impl;

import com.example.service.BankingKnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
@Order(1) // Run after DataSeeder
public class BankingKnowledgeServiceImpl implements BankingKnowledgeService, CommandLineRunner {

    private final VectorStore vectorStore;
    private boolean initialized = false;

    @Override
    public void run(String... args) {
        if (!initialized) {
            try {
                log.info("Initializing banking knowledge base...");
                initializeKnowledgeBase();
                initialized = true;
                log.info("Banking knowledge base initialized successfully!");
            } catch (Exception e) {
                log.error("Failed to initialize banking knowledge base. " +
                        "This may be due to missing Ollama embedding model. " +
                        "Please ensure 'nomic-embed-text' is available: ollama pull nomic-embed-text", e);
                // Don't fail application startup - allow it to continue without knowledge base
                log.warn("Application will continue without pre-loaded banking knowledge base.");
            }
        }
    }

    @Override
    public void initializeKnowledgeBase() {
        // Banking Regulations and Compliance
        addDocument("""
                Anti-Money Laundering (AML) Regulations:
                - Banks must report transactions over $10,000 to regulatory authorities
                - Suspicious activity reports (SARs) must be filed within 30 days
                - Customer due diligence (CDD) is required for all new accounts
                - Enhanced due diligence (EDD) is required for high-risk customers
                - Ongoing monitoring of customer transactions is mandatory
                - Banks must maintain records for at least 5 years
                """, "category:compliance,type:AML");

        addDocument("""
                Know Your Customer (KYC) Requirements:
                - Verify customer identity using government-issued ID
                - Verify address using utility bills or bank statements
                - Screen customers against sanctions lists
                - Assess customer risk profile (low, medium, high)
                - Politically Exposed Persons (PEPs) require enhanced screening
                - Regular KYC reviews required for high-risk customers
                """, "category:compliance,type:KYC");

        addDocument("""
                Fraud Detection Best Practices:
                - Monitor for unusual transaction patterns
                - Flag transactions significantly above customer's normal spending
                - Watch for rapid-fire transactions in short time periods
                - Identify transactions in unusual geographic locations
                - Detect transactions outside normal business hours
                - Monitor for account takeover indicators
                - Check for card-not-present fraud patterns
                """, "category:fraud,type:detection");

        addDocument("""
                Risk Assessment Criteria:
                - Transaction amount relative to account balance
                - Frequency and pattern of transactions
                - Geographic location and cross-border transactions
                - Merchant category codes (MCC) analysis
                - Time-of-day and day-of-week patterns
                - Customer's historical behavior
                - Account age and relationship length
                - Credit score and financial stability
                """, "category:risk,type:assessment");

        addDocument("""
                Transaction Monitoring Rules:
                - Large transactions: >$10,000 require additional verification
                - Rapid transactions: Multiple transactions within 1 hour
                - Geographic anomalies: Transactions in countries not visited before
                - Off-hours activity: Transactions between 2 AM - 5 AM
                - Velocity checks: Unusual number of transactions in short period
                - Amount patterns: Transactions just below reporting thresholds
                - Merchant patterns: Transactions with high-risk merchants
                """, "category:fraud,type:monitoring");

        addDocument("""
                Customer Service Banking Policies:
                - Account balance inquiries can be provided after identity verification
                - Transaction history available for last 90 days online, 7 years on request
                - Wire transfers require additional verification for amounts >$3,000
                - International transfers may take 3-5 business days
                - Dispute resolution: Customers have 60 days to dispute unauthorized transactions
                - Account closure requires zero balance and no pending transactions
                - Overdraft protection available for eligible accounts
                """, "category:service,type:policies");

        addDocument("""
                Regulatory Compliance Types:
                - AML (Anti-Money Laundering): Prevents money laundering activities
                - KYC (Know Your Customer): Customer identification and verification
                - Sanctions Screening: Checking against OFAC and other sanctions lists
                - GDPR: Data protection and privacy regulations
                - PCI DSS: Payment card industry data security standards
                - SOX: Financial reporting and internal controls
                - Basel III: Capital adequacy and risk management
                """, "category:compliance,type:regulations");

        addDocument("""
                High-Risk Transaction Indicators:
                - Transactions to/from high-risk jurisdictions
                - Transactions with cryptocurrency exchanges
                - Large cash deposits or withdrawals
                - Structuring (transactions just below reporting thresholds)
                - Rapid movement of funds between accounts
                - Transactions with shell companies
                - Transactions involving PEPs (Politically Exposed Persons)
                - Transactions with sanctioned entities or countries
                """, "category:risk,type:indicators");

        addDocument("""
                Account Security Measures:
                - Multi-factor authentication (MFA) required for online banking
                - Biometric authentication available for mobile apps
                - Real-time fraud alerts via SMS and email
                - Card controls: Ability to freeze/unfreeze cards
                - Transaction limits: Daily and per-transaction limits
                - Suspicious activity automatic account restrictions
                - Regular security updates and patches
                - Customer education on phishing and scams
                """, "category:security,type:measures");

        addDocument("""
                Transaction Categories and Risk Levels:
                - Low Risk: Regular purchases, recurring bills, salary deposits
                - Medium Risk: Large purchases, international transactions, new merchants
                - High Risk: Cash advances, gambling, cryptocurrency, high-risk merchants
                - Merchant Categories: Retail (low), Travel (medium), Adult (high), Gambling (high)
                - Geographic Risk: Domestic (low), International (medium), High-risk countries (high)
                """, "category:risk,type:categories");

        log.info("Loaded {} banking knowledge documents into vector store", 10);
    }

    @Override
    public void addDocument(String content, String metadata) {
        Map<String, Object> metadataMap = parseMetadata(metadata);
        Document document = new Document(content, metadataMap);
        vectorStore.add(List.of(document));
    }

    @Override
    public List<Document> searchSimilar(String query, int topK) {
        // VectorStore.similaritySearch API varies by Spring AI version
        // Try different API signatures for compatibility
        try {
            // Method 1: Try SearchRequest (newer API)
            try {
                org.springframework.ai.vectorstore.SearchRequest searchRequest = 
                    org.springframework.ai.vectorstore.SearchRequest.query(query)
                        .withTopK(topK);
                return vectorStore.similaritySearch(searchRequest);
            } catch (NoSuchMethodError | NoClassDefFoundError e) {
                // SearchRequest might not be available in this version
                log.debug("SearchRequest not available, trying alternative", e);
            }
            
            // Method 2: Try with topK parameter (if available)
            try {
                java.lang.reflect.Method method = vectorStore.getClass()
                    .getMethod("similaritySearch", String.class, int.class);
                @SuppressWarnings("unchecked")
                List<Document> results = (List<Document>) method.invoke(vectorStore, query, topK);
                return results;
            } catch (Exception e) {
                log.debug("similaritySearch(String, int) not available", e);
            }
            
            // Method 3: Try direct similaritySearch and limit results
            List<Document> allResults = vectorStore.similaritySearch(query);
            return allResults.stream().limit(topK).toList();
            
        } catch (Exception e) {
            log.error("All similaritySearch attempts failed", e);
            return List.of();
        }
    }

    private Map<String, Object> parseMetadata(String metadata) {
        Map<String, Object> metadataMap = new HashMap<>();
        if (metadata != null && !metadata.isEmpty()) {
            String[] pairs = metadata.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    metadataMap.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        return metadataMap;
    }
}

