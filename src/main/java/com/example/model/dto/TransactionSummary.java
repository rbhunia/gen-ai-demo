package com.example.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {
    private Integer totalTransactions;
    private BigDecimal totalAmount;
    private BigDecimal averageTransaction;
    private List<String> topCategories;
    private String spendingTrend; // INCREASING, DECREASING, STABLE
    private List<String> insights;
}

