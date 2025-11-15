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
public class ComplianceCheckRequest {
    @NotNull
    private String accountNumber;
    
    @NotNull
    private String customerId;
    
    @NotNull
    private String complianceType; // AML, KYC, SANCTIONS, REGULATORY
}

