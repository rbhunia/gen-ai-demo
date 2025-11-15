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
public class FraudAnalysisResult {
    private Integer riskScore;
    private Boolean isFraudulent;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private List<String> reasons;
    private String recommendation;
}

