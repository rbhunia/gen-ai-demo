package com.example.controller;

import com.example.model.dto.DocumentSearchRequest;
import com.example.model.dto.DocumentSearchResponse;
import com.example.service.DocumentSearchService;
import com.example.annotation.RateLimited;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentSearchController {

    private final DocumentSearchService documentSearchService;

    @PostMapping("/index")
    public ResponseEntity<Map<String, String>> indexDocument(
            @RequestParam String documentId,
            @RequestParam String content,
            @RequestParam(required = false) String metadata) {
        log.info("Indexing document: {}", documentId);
        documentSearchService.indexDocument(documentId, content, metadata != null ? metadata : "");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", "success", "documentId", documentId));
    }

    @PostMapping("/index/file")
    public ResponseEntity<Map<String, String>> indexDocumentFromFile(
            @RequestParam String documentId,
            @RequestParam MultipartFile file) {
        log.info("Indexing document from file: {}", documentId);
        documentSearchService.indexDocumentFromFile(documentId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", "success", "documentId", documentId));
    }

    @PostMapping("/search")
    @RateLimited(service = "document-search")
    public ResponseEntity<DocumentSearchResponse> searchDocuments(
            @RequestBody @Valid DocumentSearchRequest request) {
        log.info("Document search: query={}", request.getQuery());
        DocumentSearchResponse response = documentSearchService.searchDocuments(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/search/semantic")
    public ResponseEntity<List<DocumentSearchResponse.DocumentResult>> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        log.info("Semantic search: query={}, topK={}", query, topK);
        List<DocumentSearchResponse.DocumentResult> results = 
                documentSearchService.semanticSearch(query, topK);
        return ResponseEntity.status(HttpStatus.OK).body(results);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable String documentId) {
        log.info("Deleting document: {}", documentId);
        documentSearchService.deleteDocument(documentId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("status", "deleted", "documentId", documentId));
    }
}
