package com.example.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchRequest {
    @NotBlank
    private String query;
    
    private Integer topK;
    
    private Double similarityThreshold;
    
    private String documentType;
    
    private List<String> tags;
}

