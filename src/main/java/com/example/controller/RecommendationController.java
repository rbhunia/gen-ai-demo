package com.example.controller;

import com.example.model.dto.RecommendationRequest;
import com.example.model.dto.RecommendationResponse;
import com.example.service.RecommendationService;
import com.example.annotation.RateLimited;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/products")
    @RateLimited(service = "recommendation")
    public ResponseEntity<RecommendationResponse> recommendProducts(
            @RequestBody @Valid RecommendationRequest request) {
        log.info("Product recommendation request for customer: {}", request.getCustomerId());
        RecommendationResponse response = recommendationService.recommendProducts(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/similar-customers/{customerId}")
    public ResponseEntity<List<String>> findSimilarCustomers(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "5") int topK) {
        log.info("Finding similar customers for: {}", customerId);
        List<String> similarCustomers = recommendationService.findSimilarCustomers(customerId, topK);
        return ResponseEntity.status(HttpStatus.OK).body(similarCustomers);
    }

    @GetMapping("/transaction-based/{accountNumber}")
    public ResponseEntity<List<String>> recommendBasedOnTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "3") int topK) {
        log.info("Transaction-based recommendations for account: {}", accountNumber);
        List<String> recommendations = recommendationService.recommendBasedOnTransactions(accountNumber, topK);
        return ResponseEntity.status(HttpStatus.OK).body(recommendations);
    }
}

