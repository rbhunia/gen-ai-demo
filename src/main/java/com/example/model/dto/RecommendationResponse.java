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
public class RecommendationResponse {
    private String customerId;
    private String recommendationType;
    private List<RecommendationItem> recommendations;
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private String itemId;
        private String itemName;
        private String category;
        private Double relevanceScore;
        private String reason;
        private Map<String, Object> details;
    }
}

