package com.example.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    @NotNull
    private String customerId;
    
    private String accountNumber;
    
    private List<String> preferences;
    
    private String recommendationType; // PRODUCTS, SERVICES, INVESTMENTS
    
    private Integer topK;
}

