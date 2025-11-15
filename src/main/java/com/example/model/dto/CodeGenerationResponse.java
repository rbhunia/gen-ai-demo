package com.example.model.dto;

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
public class CodeGenerationResponse {
    private String generatedCode;
    private String language;
    private String explanation;
    private List<String> suggestions;
    private Map<String, Object> metadata;
    private Long generationTimeMs;
}
