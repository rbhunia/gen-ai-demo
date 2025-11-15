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
public class ComplianceCheckResponse {
    private String accountNumber;
    private String customerId;
    private String complianceType;
    private String status;
    private String aiAnalysis;
    private List<String> findings;
    private List<String> recommendations;
}

