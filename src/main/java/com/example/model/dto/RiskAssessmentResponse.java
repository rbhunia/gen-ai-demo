package com.example.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentResponse {
    private String accountNumber;
    private String customerId;
    private String overallRiskLevel;
    private Double overallRiskScore;
    private String aiAnalysis;
    private List<String> riskFactors;
    private List<String> recommendations;
}

