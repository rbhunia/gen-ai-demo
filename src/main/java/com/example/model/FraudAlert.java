package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long transactionId;
    
    @Column(nullable = false)
    private String accountNumber;
    
    @Column(nullable = false)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiAnalysis;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String riskFactors;
    
    @Column(nullable = false)
    private Double riskScore; // 0.0 to 1.0
    
    @Column(nullable = false)
    private String status; // PENDING, REVIEWED, RESOLVED, FALSE_POSITIVE
    
    @Column(nullable = false)
    private LocalDateTime detectedAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private String reviewedBy;
    
    private LocalDateTime reviewedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (detectedAt == null) {
            detectedAt = LocalDateTime.now();
        }
    }
}

