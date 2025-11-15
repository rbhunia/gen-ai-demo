package com.example.controller;

import com.example.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/embeddings")
@RequiredArgsConstructor
@Slf4j
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateEmbedding(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        log.info("Generating embedding for text length: {}", text != null ? text.length() : 0);
        
        List<Double> embedding = embeddingService.generateEmbedding(text);
        
        Map<String, Object> response = new HashMap<>();
        response.put("text", text);
        response.put("embedding", embedding);
        response.put("dimension", embedding.size());
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/similarity")
    public ResponseEntity<Map<String, Object>> calculateSimilarity(
            @RequestBody Map<String, List<Double>> request) {
        List<Double> embedding1 = request.get("embedding1");
        List<Double> embedding2 = request.get("embedding2");
        
        double similarity = embeddingService.cosineSimilarity(embedding1, embedding2);
        
        Map<String, Object> response = new HashMap<>();
        response.put("similarity", similarity);
        response.put("embedding1Dimension", embedding1.size());
        response.put("embedding2Dimension", embedding2.size());
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

