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
public class RiskAssessmentResult {
    private Integer overallRiskScore;
    private String riskCategory; // LOW, MEDIUM, HIGH
    private List<RiskFactor> factors;
    private List<String> recommendations;
    private Boolean requiresReview;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskFactor {
        private String name;
        private Integer score;
    }
}

