package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String accountNumber;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false)
    private String complianceType; // AML, KYC, SANCTIONS, REGULATORY
    
    @Column(nullable = false)
    private String status; // COMPLIANT, NON_COMPLIANT, REQUIRES_REVIEW
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiAnalysis;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String findings;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendations;
    
    @Column(nullable = false)
    private LocalDateTime reportDate;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (reportDate == null) {
            reportDate = LocalDateTime.now();
        }
    }
}

