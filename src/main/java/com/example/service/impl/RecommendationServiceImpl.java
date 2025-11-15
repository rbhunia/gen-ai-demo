package com.example.service.impl;

import com.example.model.Account;
import com.example.model.Customer;
import com.example.model.Transaction;
import com.example.model.dto.RecommendationRequest;
import com.example.model.dto.RecommendationResponse;
import com.example.repository.AccountRepository;
import com.example.repository.CustomerRepository;
import com.example.repository.TransactionRepository;
import com.example.service.EmbeddingService;
import com.example.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final ChatClient chatClient;
    private final EmbeddingService embeddingService;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Banking products with embeddings (in production, these would be in a vector store)
    private static final Map<String, Map<String, Object>> BANKING_PRODUCTS = Map.of(
            "premium-checking", Map.of(
                    "name", "Premium Checking Account",
                    "category", "CHECKING",
                    "description", "High-yield checking account with premium benefits",
                    "minBalance", 5000,
                    "features", List.of("No monthly fees", "Free ATM withdrawals", "Interest earning")
            ),
            "savings-plus", Map.of(
                    "name", "Savings Plus Account",
                    "category", "SAVINGS",
                    "description", "High-interest savings account with flexible terms",
                    "minBalance", 1000,
                    "features", List.of("Competitive interest rates", "Easy transfers", "FDIC insured")
            ),
            "credit-card-rewards", Map.of(
                    "name", "Rewards Credit Card",
                    "category", "CREDIT",
                    "description", "Cashback and travel rewards credit card",
                    "minBalance", 0,
                    "features", List.of("2% cashback", "Travel rewards", "No annual fee first year")
            ),
            "investment-advisory", Map.of(
                    "name", "Investment Advisory Service",
                    "category", "INVESTMENT",
                    "description", "Professional investment management and advisory",
                    "minBalance", 25000,
                    "features", List.of("Personal advisor", "Diversified portfolio", "Tax optimization")
            ),
            "mortgage-loan", Map.of(
                    "name", "Mortgage Loan",
                    "category", "LOAN",
                    "description", "Competitive mortgage rates for home purchase or refinance",
                    "minBalance", 0,
                    "features", List.of("Low interest rates", "Flexible terms", "Quick approval")
            )
    );

    @Override
    public RecommendationResponse recommendProducts(RecommendationRequest request) {
        log.info("Generating product recommendations for customer: {}", request.getCustomerId());

        var customer = customerRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Build customer profile
        String customerProfile = buildCustomerProfile(customer, request);

        // Generate recommendations using AI
        String recommendationPrompt = """
                You are a banking product recommendation expert.
                Based on the following customer profile, recommend the most suitable banking products.
                
                Customer Profile:
                {customerProfile}
                
                Available Products:
                {availableProducts}
                
                Provide recommendations in the following format:
                PRODUCT_ID: [id]
                REASON: [why this product is recommended]
                RELEVANCE_SCORE: [0.0 to 1.0]
                
                Provide top {topK} recommendations.
                """;

        PromptTemplate template = new PromptTemplate(recommendationPrompt);
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerProfile", customerProfile);
        variables.put("availableProducts", formatProducts());
        variables.put("topK", request.getTopK() != null ? request.getTopK() : 3);

        Prompt prompt = template.create(variables);
        String aiResponse = chatClient.prompt(prompt).call().content();

        // Parse recommendations
        List<RecommendationResponse.RecommendationItem> items = parseRecommendations(aiResponse);

        return RecommendationResponse.builder()
                .customerId(request.getCustomerId())
                .recommendationType(request.getRecommendationType() != null ? request.getRecommendationType() : "PRODUCTS")
                .recommendations(items)
                .metadata(Map.of("generatedAt", new Date(), "totalProducts", BANKING_PRODUCTS.size()))
                .build();
    }

    @Override
    public List<String> findSimilarCustomers(String customerId, int topK) {
        log.info("Finding similar customers for: {}", customerId);

        var customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Build customer embedding
        String customerProfile = buildCustomerProfile(customer, null);
        List<Double> customerEmbedding = embeddingService.generateEmbedding(customerProfile);

        // Find similar customers using embeddings
        List<Customer> allCustomers = customerRepository.findAll();
        Map<String, Double> similarities = new HashMap<>();

        for (Customer otherCustomer : allCustomers) {
            if (otherCustomer.getCustomerId().equals(customerId)) {
                continue;
            }
            String otherProfile = buildCustomerProfile(otherCustomer, null);
            List<Double> otherEmbedding = embeddingService.generateEmbedding(otherProfile);
            double similarity = embeddingService.cosineSimilarity(customerEmbedding, otherEmbedding);
            similarities.put(otherCustomer.getCustomerId(), similarity);
        }

        return similarities.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> recommendBasedOnTransactions(String accountNumber, int topK) {
        log.info("Generating recommendations based on transactions for account: {}", accountNumber);

        var account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        List<Transaction> transactions = transactionRepository
                .findRecentTransactionsByAccount(accountNumber, 
                        java.time.LocalDateTime.now().minusMonths(6));

        // Build transaction pattern embedding
        String transactionPattern = buildTransactionPattern(transactions);
        List<Double> patternEmbedding = embeddingService.generateEmbedding(transactionPattern);

        // Compare with product embeddings
        Map<String, Double> productScores = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> product : BANKING_PRODUCTS.entrySet()) {
            String productDescription = (String) product.getValue().get("description");
            List<Double> productEmbedding = embeddingService.generateEmbedding(productDescription);
            double similarity = embeddingService.cosineSimilarity(patternEmbedding, productEmbedding);
            productScores.put(product.getKey(), similarity);
        }

        return productScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private String buildCustomerProfile(Customer customer, RecommendationRequest request) {
        StringBuilder profile = new StringBuilder();
        profile.append(String.format("Customer ID: %s\n", customer.getCustomerId()));
        profile.append(String.format("Name: %s %s\n", customer.getFirstName(), customer.getLastName()));
        profile.append(String.format("Risk Profile: %s\n", customer.getRiskProfile()));
        profile.append(String.format("KYC Status: %s\n", customer.getKycStatus()));

        if (request != null && request.getAccountNumber() != null) {
            accountRepository.findByAccountNumber(request.getAccountNumber())
                    .ifPresent(account -> {
                        profile.append(String.format("Account Type: %s\n", account.getAccountType()));
                        profile.append(String.format("Balance: %s\n", account.getBalance()));
                    });
        }

        if (request != null && request.getPreferences() != null) {
            profile.append(String.format("Preferences: %s\n", String.join(", ", request.getPreferences())));
        }

        return profile.toString();
    }

    private String buildTransactionPattern(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "No transaction history";
        }

        Map<String, Long> categoryCount = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getMerchantCategory, Collectors.counting()));

        StringBuilder pattern = new StringBuilder("Transaction patterns: ");
        categoryCount.forEach((category, count) ->
                pattern.append(String.format("%s (%d), ", category, count)));

        return pattern.toString();
    }

    private String formatProducts() {
        StringBuilder sb = new StringBuilder();
        BANKING_PRODUCTS.forEach((id, details) -> {
            sb.append(String.format("- %s: %s - %s\n", id, details.get("name"), details.get("description")));
        });
        return sb.toString();
    }

    private List<RecommendationResponse.RecommendationItem> parseRecommendations(String aiResponse) {
        List<RecommendationResponse.RecommendationItem> items = new ArrayList<>();
        String[] lines = aiResponse.split("\n");

        String currentProductId = null;
        String currentReason = null;
        Double currentScore = 0.5;

        for (String line : lines) {
            if (line.startsWith("PRODUCT_ID:")) {
                if (currentProductId != null) {
                    items.add(createRecommendationItem(currentProductId, currentReason, currentScore));
                }
                currentProductId = line.substring("PRODUCT_ID:".length()).trim();
            } else if (line.startsWith("REASON:")) {
                currentReason = line.substring("REASON:".length()).trim();
            } else if (line.startsWith("RELEVANCE_SCORE:")) {
                try {
                    currentScore = Double.parseDouble(line.substring("RELEVANCE_SCORE:".length()).trim());
                } catch (NumberFormatException e) {
                    currentScore = 0.5;
                }
            }
        }

        if (currentProductId != null) {
            items.add(createRecommendationItem(currentProductId, currentReason, currentScore));
        }

        return items.isEmpty() ? getDefaultRecommendations() : items;
    }

    private RecommendationResponse.RecommendationItem createRecommendationItem(
            String productId, String reason, Double score) {
        Map<String, Object> productDetails = BANKING_PRODUCTS.getOrDefault(productId, Map.of());
        return RecommendationResponse.RecommendationItem.builder()
                .itemId(productId)
                .itemName((String) productDetails.getOrDefault("name", productId))
                .category((String) productDetails.getOrDefault("category", "UNKNOWN"))
                .relevanceScore(score)
                .reason(reason != null ? reason : "Recommended based on your profile")
                .details(productDetails)
                .build();
    }

    private List<RecommendationResponse.RecommendationItem> getDefaultRecommendations() {
        return List.of(
                createRecommendationItem("premium-checking", "Suitable for your account type", 0.7),
                createRecommendationItem("savings-plus", "Good savings option", 0.6)
        );
    }
}

