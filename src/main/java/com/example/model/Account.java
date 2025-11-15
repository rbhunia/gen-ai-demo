package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String accountNumber;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false)
    private String accountType; // CHECKING, SAVINGS, CREDIT, INVESTMENT
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    
    @Column(nullable = false)
    private String currency;
    
    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, FROZEN, CLOSED
    
    private BigDecimal creditLimit;
    
    @Column(nullable = false)
    private LocalDateTime openedDate;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (openedDate == null) {
            openedDate = LocalDateTime.now();
        }
    }
}

