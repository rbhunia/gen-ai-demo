package com.example.config;

import com.example.model.Account;
import com.example.model.Customer;
import com.example.model.Transaction;
import com.example.repository.AccountRepository;
import com.example.repository.CustomerRepository;
import com.example.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    @Override
    public void run(String... args) {
        if (customerRepository.count() == 0) {
            log.info("Seeding initial data...");
            seedData();
            log.info("Data seeding completed!");
        } else {
            log.info("Data already exists, skipping seed.");
        }
    }
    
    private void seedData() {
        // Create Customers
        Customer customer1 = Customer.builder()
                .customerId("CUST001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1-555-0101")
                .dateOfBirth(java.time.LocalDate.of(1985, 5, 15))
                .address("123 Main Street")
                .city("New York")
                .country("USA")
                .kycStatus("VERIFIED")
                .riskProfile("LOW")
                .build();
        
        Customer customer2 = Customer.builder()
                .customerId("CUST002")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("+1-555-0102")
                .dateOfBirth(java.time.LocalDate.of(1990, 8, 22))
                .address("456 Oak Avenue")
                .city("Los Angeles")
                .country("USA")
                .kycStatus("VERIFIED")
                .riskProfile("MEDIUM")
                .build();
        
        Customer customer3 = Customer.builder()
                .customerId("CUST003")
                .firstName("Robert")
                .lastName("Johnson")
                .email("robert.j@example.com")
                .phoneNumber("+1-555-0103")
                .dateOfBirth(java.time.LocalDate.of(1978, 3, 10))
                .address("789 Pine Road")
                .city("Chicago")
                .country("USA")
                .kycStatus("VERIFIED")
                .riskProfile("HIGH")
                .build();
        
        customerRepository.saveAll(List.of(customer1, customer2, customer3));
        
        // Create Accounts
        Account account1 = Account.builder()
                .accountNumber("ACC001")
                .customerId("CUST001")
                .accountType("CHECKING")
                .balance(new BigDecimal("5000.00"))
                .currency("USD")
                .status("ACTIVE")
                .build();
        
        Account account2 = Account.builder()
                .accountNumber("ACC002")
                .customerId("CUST001")
                .accountType("SAVINGS")
                .balance(new BigDecimal("25000.00"))
                .currency("USD")
                .status("ACTIVE")
                .build();
        
        Account account3 = Account.builder()
                .accountNumber("ACC003")
                .customerId("CUST002")
                .accountType("CHECKING")
                .balance(new BigDecimal("3500.00"))
                .currency("USD")
                .status("ACTIVE")
                .build();
        
        Account account4 = Account.builder()
                .accountNumber("ACC004")
                .customerId("CUST003")
                .accountType("CHECKING")
                .balance(new BigDecimal("15000.00"))
                .currency("USD")
                .status("ACTIVE")
                .creditLimit(new BigDecimal("10000.00"))
                .build();
        
        accountRepository.saveAll(List.of(account1, account2, account3, account4));
        
        // Create Transactions
        LocalDateTime now = LocalDateTime.now();
        
        List<Transaction> transactions = List.of(
                Transaction.builder()
                        .accountNumber("ACC001")
                        .transactionType("DEBIT")
                        .amount(new BigDecimal("150.00"))
                        .currency("USD")
                        .merchantName("Starbucks")
                        .merchantCategory("FOOD_AND_BEVERAGE")
                        .location("New York, NY")
                        .transactionDate(now.minusDays(1))
                        .status("COMPLETED")
                        .description("Coffee purchase")
                        .build(),
                Transaction.builder()
                        .accountNumber("ACC001")
                        .transactionType("DEBIT")
                        .amount(new BigDecimal("2500.00"))
                        .currency("USD")
                        .merchantName("Best Buy")
                        .merchantCategory("ELECTRONICS")
                        .location("New York, NY")
                        .transactionDate(now.minusDays(2))
                        .status("COMPLETED")
                        .description("Electronics purchase")
                        .build(),
                Transaction.builder()
                        .accountNumber("ACC001")
                        .transactionType("CREDIT")
                        .amount(new BigDecimal("5000.00"))
                        .currency("USD")
                        .merchantName("Salary Deposit")
                        .merchantCategory("SALARY")
                        .location("New York, NY")
                        .transactionDate(now.minusDays(5))
                        .status("COMPLETED")
                        .description("Monthly salary")
                        .build(),
                Transaction.builder()
                        .accountNumber("ACC003")
                        .transactionType("DEBIT")
                        .amount(new BigDecimal("50000.00"))
                        .currency("USD")
                        .merchantName("Unknown Merchant")
                        .merchantCategory("OTHER")
                        .location("Unknown Location")
                        .transactionDate(now.minusHours(2))
                        .status("PENDING")
                        .description("Large transaction")
                        .build(),
                Transaction.builder()
                        .accountNumber("ACC004")
                        .transactionType("DEBIT")
                        .amount(new BigDecimal("12000.00"))
                        .currency("USD")
                        .merchantName("International Transfer")
                        .merchantCategory("TRANSFER")
                        .location("Offshore")
                        .transactionDate(now.minusHours(1))
                        .status("PENDING")
                        .description("International wire transfer")
                        .counterpartyAccount("OFFSHORE001")
                        .build()
        );
        
        transactionRepository.saveAll(transactions);
    }
}

