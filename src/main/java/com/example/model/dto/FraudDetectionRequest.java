package com.example.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionRequest {
    @NotNull
    private String accountNumber;
    
    @NotNull
    private String transactionType;
    
    @NotNull
    private BigDecimal amount;
    
    @NotNull
    private String currency;
    
    @NotNull
    private String merchantName;
    
    @NotNull
    private String merchantCategory;
    
    @NotNull
    private String location;
    
    @NotNull
    private LocalDateTime transactionDate;
    
    private String description;
    
    private String counterpartyAccount;
}

