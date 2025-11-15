package com.example.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAnalysisResponse {
    private String accountNumber;
    private String analysisType;
    private String aiInsights;
    private Map<String, BigDecimal> categoryBreakdown;
    private List<String> keyFindings;
    private List<String> recommendations;
    private Map<String, Object> statistics;
}

