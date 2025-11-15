package com.example.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedPromptResponse {
    private String optimizedPrompt;
    private String technique;
    private String explanation;
    private Map<String, Object> metadata;
    private Integer estimatedTokens;
}

