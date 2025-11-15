package com.example.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedPromptRequest {
    @NotBlank
    private String basePrompt;
    
    private String technique; // CHAIN_OF_THOUGHT, FEW_SHOT, ROLE_BASED, ZERO_SHOT, etc.
    
    private String role;
    
    private List<String> examples;
    
    private String optimizationGoal; // CLARITY, BREVITY, ACCURACY, etc.
    
    private Map<String, String> context;
    
    private String outputFormat; // JSON, XML, STRUCTURED, etc.
}

