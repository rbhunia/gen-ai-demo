package com.example.controller;

import com.example.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
@Slf4j
public class RAGController {
    
    private final RAGService ragService;
    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchKnowledge(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        log.info("RAG knowledge search: query={}, topK={}", query, topK);
        
        String context = ragService.retrieveRelevantContext(query, topK);
        List<String> contexts = ragService.retrieveRelevantContexts(query, topK);
        
        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("topK", topK);
        response.put("context", context);
        response.put("contexts", contexts);
        response.put("count", contexts.size());
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

