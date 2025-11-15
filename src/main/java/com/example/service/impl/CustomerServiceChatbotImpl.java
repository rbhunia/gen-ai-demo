package com.example.service.impl;

import com.example.model.Account;
import com.example.model.Customer;
import com.example.model.Transaction;
import com.example.model.dto.ChatRequest;
import com.example.model.dto.ChatResponse;
import com.example.repository.AccountRepository;
import com.example.repository.CustomerRepository;
import com.example.repository.TransactionRepository;
import com.example.service.CustomerServiceChatbot;
import com.example.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceChatbotImpl implements CustomerServiceChatbot {
    
    private final ChatClient chatClient;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RAGService ragService;
    
    @Override
    public ChatResponse handleCustomerInquiry(ChatRequest request) {
        log.info("Handling customer inquiry: customerId={}, context={}", 
                request.getCustomerId(), request.getContext());
        
        // Build context based on customer and account information
        String bankingContext = buildBankingContext(request);
        
        // Retrieve relevant banking knowledge using RAG
        String ragContext = ragService.retrieveRelevantContext(request.getMessage(), 3);
        
        // Create specialized prompt for banking customer service
        String systemPrompt = """
                You are a helpful and professional banking customer service assistant.
                You have access to customer account information and transaction history.
                Always be polite, accurate, and helpful. If you don't have specific information,
                guide the customer on how to obtain it or escalate to a human agent.
                
                Important guidelines:
                - Never share sensitive information like full account numbers or PINs
                - Always verify customer identity before discussing account details
                - Be clear about transaction limits, fees, and policies
                - If a question requires human intervention, clearly state that
                - Provide accurate information based on the context provided
                
                Banking Knowledge Base:
                {ragContext}
                
                Customer Context:
                {bankingContext}
                """;
        
        String userPrompt = request.getMessage();
        
        PromptTemplate promptTemplate = new PromptTemplate(systemPrompt + "\n\nCustomer Question: {userMessage}");
        Map<String, Object> variables = new HashMap<>();
        variables.put("ragContext", ragContext);
        variables.put("bankingContext", bankingContext);
        variables.put("userMessage", userPrompt);
        
        Prompt prompt = promptTemplate.create(variables);
        String aiResponse = chatClient.prompt(prompt).call().content();
        
        // Determine if human agent is needed
        boolean requiresHumanAgent = shouldEscalateToHuman(aiResponse, request.getMessage());
        
        return ChatResponse.builder()
                .response(aiResponse)
                .customerId(request.getCustomerId())
                .timestamp(LocalDateTime.now())
                .context(request.getContext() != null ? request.getContext() : "GENERAL")
                .requiresHumanAgent(requiresHumanAgent)
                .build();
    }
    
    private String buildBankingContext(ChatRequest request) {
        StringBuilder context = new StringBuilder();
        
        if (request.getCustomerId() != null) {
            Optional<Customer> customerOpt = customerRepository.findByCustomerId(request.getCustomerId());
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                context.append(String.format("""
                        Customer Information:
                        - Name: %s %s
                        - Account Status: Active
                        - KYC Status: %s
                        - Risk Profile: %s
                        
                        """, customer.getFirstName(), customer.getLastName(), 
                        customer.getKycStatus(), customer.getRiskProfile()));
            }
        }
        
        if (request.getAccountNumber() != null) {
            Optional<Account> accountOpt = accountRepository.findByAccountNumber(request.getAccountNumber());
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                context.append(String.format("""
                        Account Information:
                        - Account Number: %s (masked: ****%s)
                        - Account Type: %s
                        - Balance: %s %s
                        - Status: %s
                        
                        """, account.getAccountNumber(), 
                        account.getAccountNumber().substring(Math.max(0, account.getAccountNumber().length() - 4)),
                        account.getAccountType(), account.getBalance(), 
                        account.getCurrency(), account.getStatus()));
                
                // Add recent transactions if context requires it
                if ("TRANSACTION_HISTORY".equals(request.getContext())) {
                    List<Transaction> recentTransactions = transactionRepository
                            .findRecentTransactionsByAccount(
                                    request.getAccountNumber(), 
                                    LocalDateTime.now().minusDays(7));
                    
                    if (!recentTransactions.isEmpty()) {
                        context.append("Recent Transactions (Last 7 days):\n");
                        recentTransactions.stream().limit(5).forEach(t -> 
                                context.append(String.format("- %s: %s %s at %s on %s\n",
                                        t.getTransactionType(), t.getAmount(), t.getCurrency(),
                                        t.getMerchantName(), t.getTransactionDate())));
                    }
                }
            }
        }
        
        if (context.isEmpty()) {
            context.append("General banking context. No specific customer or account information available.\n");
        }
        
        return context.toString();
    }
    
    private boolean shouldEscalateToHuman(String aiResponse, String userMessage) {
        String lowerResponse = aiResponse.toLowerCase();
        String lowerMessage = userMessage.toLowerCase();
        
        // Escalate if AI indicates it can't help or if sensitive operations are requested
        return lowerResponse.contains("human agent") ||
               lowerResponse.contains("escalate") ||
               lowerResponse.contains("cannot help") ||
               lowerMessage.contains("close account") ||
               lowerMessage.contains("dispute") ||
               lowerMessage.contains("fraud") ||
               lowerMessage.contains("complaint");
    }
}

