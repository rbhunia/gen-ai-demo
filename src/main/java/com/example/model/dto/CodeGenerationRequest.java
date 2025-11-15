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
public class CodeGenerationRequest {
    @NotBlank
    private String description;
    
    @NotBlank
    private String language; // JAVA, PYTHON, JAVASCRIPT, etc.
    
    private String framework; // SPRING_BOOT, REACT, etc.
    
    private String style; // CLEAN_CODE, PERFORMANCE_OPTIMIZED, etc.
    
    private String context; // Additional context or requirements
}

