package com.example.service;

import com.example.model.dto.RecommendationRequest;
import com.example.model.dto.RecommendationResponse;

import java.util.List;

public interface RecommendationService {
    RecommendationResponse recommendProducts(RecommendationRequest request);
    List<String> findSimilarCustomers(String customerId, int topK);
    List<String> recommendBasedOnTransactions(String accountNumber, int topK);
}

