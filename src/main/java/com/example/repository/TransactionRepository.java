package com.example.repository;

import com.example.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountNumber(String accountNumber);
    
    List<Transaction> findByAccountNumberAndTransactionDateBetween(
            String accountNumber, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Transaction> findByStatus(String status);
    
    @Query("SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber " +
           "AND t.transactionDate >= :startDate ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactionsByAccount(
            @Param("accountNumber") String accountNumber,
            @Param("startDate") LocalDateTime startDate);
    
    List<Transaction> findByMerchantCategory(String merchantCategory);
}

