package com.example.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionResponse {
    private Long transactionId;
    private String accountNumber;
    private String severity;
    private String aiAnalysis;
    private String riskFactors;
    private Double riskScore;
    private String recommendation;
    private Boolean isFraudulent;
}

