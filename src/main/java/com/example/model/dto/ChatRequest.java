package com.example.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @NotBlank(message = "Message is required")
    private String message;
    
    private String customerId;
    
    private String accountNumber;
    
    private String context; // ACCOUNT_INFO, TRANSACTION_HISTORY, GENERAL
}

