package com.example.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAnalysisRequest {
    @NotNull
    private String accountNumber;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private String analysisType; // SPENDING_PATTERNS, CATEGORY_BREAKDOWN, TRENDS, ANOMALIES
}

