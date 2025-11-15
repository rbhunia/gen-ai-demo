package com.example.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentRequest {
    @NotNull
    private String accountNumber;
    
    @NotNull
    private String customerId;
    
    private Boolean includeTransactionHistory;
    
    private Boolean includeComplianceCheck;
}

