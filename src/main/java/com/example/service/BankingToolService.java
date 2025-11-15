package com.example.service;

import com.example.model.Account;
import com.example.model.Transaction;
import com.example.repository.AccountRepository;
import com.example.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Banking Tool Service - Functions that can be called by AI
 * This demonstrates Spring AI Function Calling / Tool Calling feature
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankingToolService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Tool: Get account balance
     * AI can call this to check account balance
     */
    public String getAccountBalance(String accountNumber) {
        log.info("AI Tool Call: getAccountBalance for account: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> String.format("Account %s balance: %s %s. Status: %s",
                        accountNumber,
                        account.getBalance(),
                        account.getCurrency(),
                        account.getStatus()))
                .orElse("Account not found: " + accountNumber);
    }

    /**
     * Tool: Get recent transactions
     * AI can call this to retrieve transaction history
     */
    public String getRecentTransactions(String accountNumber, int limit) {
        log.info("AI Tool Call: getRecentTransactions for account: {}, limit: {}", accountNumber, limit);
        List<Transaction> transactions = transactionRepository
                .findByAccountNumber(accountNumber)
                .stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .limit(Math.min(limit, 10))
                .collect(Collectors.toList());

        if (transactions.isEmpty()) {
            return "No recent transactions found for account: " + accountNumber;
        }

        StringBuilder result = new StringBuilder("Recent transactions:\n");
        transactions.forEach(tx -> result.append(String.format(
                "- %s: %s %s on %s (%s)\n",
                tx.getTransactionType(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getTransactionDate(),
                tx.getDescription() != null ? tx.getDescription() : "N/A"
        )));
        return result.toString();
    }

    /**
     * Tool: Check account status
     * AI can call this to verify account status
     */
    public String checkAccountStatus(String accountNumber) {
        log.info("AI Tool Call: checkAccountStatus for account: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> String.format(
                        "Account %s: Status=%s, Type=%s, Balance=%s %s, Opened=%s",
                        accountNumber,
                        account.getStatus(),
                        account.getAccountType(),
                        account.getBalance(),
                        account.getCurrency(),
                        account.getOpenedDate()))
                .orElse("Account not found: " + accountNumber);
    }

    /**
     * Tool: Calculate spending by category
     * AI can call this to analyze spending patterns
     */
    public String calculateSpendingByCategory(String accountNumber, int days) {
        log.info("AI Tool Call: calculateSpendingByCategory for account: {}, days: {}", accountNumber, days);
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        List<Transaction> transactions = transactionRepository
                .findByAccountNumber(accountNumber)
                .stream()
                .filter(tx -> tx.getTransactionDate().isAfter(startDate))
                .collect(Collectors.toList());

        if (transactions.isEmpty()) {
            return String.format("No transactions found for account %s in the last %d days", accountNumber, days);
        }

        var categorySpending = transactions.stream()
                .filter(tx -> "DEBIT".equals(tx.getTransactionType()))
                .collect(Collectors.groupingBy(
                        Transaction::getMerchantCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        StringBuilder result = new StringBuilder(String.format("Spending by category (last %d days):\n", days));
        categorySpending.forEach((category, total) -> 
                result.append(String.format("- %s: %s\n", category, total)));

        return result.toString();
    }

    /**
     * Tool: Get account summary
     * AI can call this to get comprehensive account information
     */
    public String getAccountSummary(String accountNumber) {
        log.info("AI Tool Call: getAccountSummary for account: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> {
                    List<Transaction> accountTransactions = transactionRepository
                            .findByAccountNumber(accountNumber);
                    long transactionCount = accountTransactions.size();
                    BigDecimal totalDebits = accountTransactions.stream()
                            .filter(tx -> "DEBIT".equals(tx.getTransactionType()))
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return String.format(
                            "Account Summary for %s:\n" +
                            "- Balance: %s %s\n" +
                            "- Status: %s\n" +
                            "- Type: %s\n" +
                            "- Total Transactions: %d\n" +
                            "- Total Spending: %s %s",
                            accountNumber,
                            account.getBalance(),
                            account.getCurrency(),
                            account.getStatus(),
                            account.getAccountType(),
                            transactionCount,
                            totalDebits,
                            account.getCurrency()
                    );
                })
                .orElse("Account not found: " + accountNumber);
    }
}

