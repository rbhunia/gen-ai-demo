package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "risk_assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String accountNumber;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false)
    private String overallRiskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    
    @Column(nullable = false)
    private Double overallRiskScore; // 0.0 to 1.0
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiAnalysis;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String riskFactors;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendations;
    
    @Column(nullable = false)
    private LocalDateTime assessmentDate;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (assessmentDate == null) {
            assessmentDate = LocalDateTime.now();
        }
    }
}

