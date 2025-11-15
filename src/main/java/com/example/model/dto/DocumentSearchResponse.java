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
public class DocumentSearchResponse {
    private String query;
    private Integer topK;
    private List<DocumentResult> results;
    private Long totalResults;
    private Double searchTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentResult {
        private String documentId;
        private String content;
        private Double similarityScore;
        private Map<String, Object> metadata;
        private String snippet;
    }
}

