package com.example.service.impl;

import com.example.model.dto.DocumentSearchRequest;
import com.example.model.dto.DocumentSearchResponse;
import com.example.service.DocumentSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentSearchServiceImpl implements DocumentSearchService {

    private final VectorStore vectorStore;
    private final Map<String, Document> documentIndex = new HashMap<>();

    @Override
    public void indexDocument(String documentId, String content, String metadata) {
        log.info("Indexing document: {}", documentId);
        Map<String, Object> metadataMap = parseMetadata(metadata);
        metadataMap.put("documentId", documentId);
        metadataMap.put("indexedAt", new Date().toString());

        Document document = new Document(documentId, content, metadataMap);
        vectorStore.add(List.of(document));
        documentIndex.put(documentId, document);
        log.info("Document indexed successfully: {}", documentId);
    }

    @Override
    public void indexDocumentFromFile(String documentId, MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String metadata = String.format("filename:%s,contentType:%s,size:%d",
                    file.getOriginalFilename(), file.getContentType(), file.getSize());
            indexDocument(documentId, content, metadata);
        } catch (IOException e) {
            log.error("Error reading file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to read file", e);
        }
    }

    @Override
    public DocumentSearchResponse searchDocuments(DocumentSearchRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Searching documents with query: {}", request.getQuery());

        int topK = request.getTopK() != null ? request.getTopK() : 5;
        double threshold = request.getSimilarityThreshold() != null ? request.getSimilarityThreshold() : 0.0;

        // Perform semantic search - handle API variations
        List<Document> documents = performSimilaritySearch(request.getQuery(), topK * 2);

        // Filter by threshold and metadata
        List<DocumentSearchResponse.DocumentResult> results = documents.stream()
                .filter(doc -> {
                    // Apply metadata filters if specified
                    if (request.getDocumentType() != null) {
                        Object docType = doc.getMetadata().get("documentType");
                        if (docType == null || !docType.toString().equals(request.getDocumentType())) {
                            return false;
                        }
                    }
                    if (request.getTags() != null && !request.getTags().isEmpty()) {
                        Object tags = doc.getMetadata().get("tags");
                        if (tags == null) {
                            return false;
                        }
                        // Simple tag matching - can be enhanced
                    }
                    return true;
                })
                .limit(topK)
                .map(doc -> {
                    double score = doc.getMetadata().containsKey("similarity") ?
                            ((Number) doc.getMetadata().get("similarity")).doubleValue() : 0.8;
                    return DocumentSearchResponse.DocumentResult.builder()
                            .documentId((String) doc.getMetadata().getOrDefault("documentId", doc.getId()))
                            .content(doc.getContent())
                            .similarityScore(score)
                            .metadata(doc.getMetadata())
                            .snippet(extractSnippet(doc.getContent(), request.getQuery()))
                            .build();
                })
                .filter(result -> result.getSimilarityScore() >= threshold)
                .collect(Collectors.toList());

        long searchTime = System.currentTimeMillis() - startTime;

        return DocumentSearchResponse.builder()
                .query(request.getQuery())
                .topK(topK)
                .results(results)
                .totalResults((long) results.size())
                .searchTimeMs((double) searchTime)
                .build();
    }

    @Override
    public List<DocumentSearchResponse.DocumentResult> semanticSearch(String query, int topK) {
        log.debug("Semantic search: query={}, topK={}", query, topK);
        List<Document> documents = performSimilaritySearch(query, topK);
        return documents.stream()
                .map(doc -> DocumentSearchResponse.DocumentResult.builder()
                        .documentId((String) doc.getMetadata().getOrDefault("documentId", doc.getId()))
                        .content(doc.getContent())
                        .similarityScore(0.8) // Default score
                        .metadata(doc.getMetadata())
                        .snippet(extractSnippet(doc.getContent(), query))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDocument(String documentId) {
        log.info("Deleting document: {}", documentId);
        documentIndex.remove(documentId);
        // Note: Vector store deletion depends on implementation
        // This is a simplified version
    }

    private Map<String, Object> parseMetadata(String metadata) {
        Map<String, Object> metadataMap = new HashMap<>();
        if (metadata != null && !metadata.isEmpty()) {
            String[] pairs = metadata.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    metadataMap.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        return metadataMap;
    }

    /**
     * Perform similarity search with API compatibility handling
     */
    private List<Document> performSimilaritySearch(String query, int topK) {
        try {
            // Method 1: Try SearchRequest (newer API)
            try {
                org.springframework.ai.vectorstore.SearchRequest searchRequest = 
                    org.springframework.ai.vectorstore.SearchRequest.query(query)
                        .withTopK(topK);
                return vectorStore.similaritySearch(searchRequest);
            } catch (NoSuchMethodError | NoClassDefFoundError e) {
                log.debug("SearchRequest not available, trying alternative", e);
            }
            
            // Method 2: Try with topK parameter using reflection
            try {
                java.lang.reflect.Method method = vectorStore.getClass()
                    .getMethod("similaritySearch", String.class, int.class);
                @SuppressWarnings("unchecked")
                List<Document> results = (List<Document>) method.invoke(vectorStore, query, topK);
                return results;
            } catch (Exception e) {
                log.debug("similaritySearch(String, int) not available", e);
            }
            
            // Method 3: Try direct similaritySearch and limit results
            List<Document> allResults = vectorStore.similaritySearch(query);
            return allResults.stream().limit(topK).toList();
            
        } catch (Exception e) {
            log.error("All similaritySearch attempts failed", e);
            return List.of();
        }
    }

    private String extractSnippet(String content, String query) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        String lowerContent = content.toLowerCase();
        String lowerQuery = query.toLowerCase();
        int index = lowerContent.indexOf(lowerQuery);

        if (index == -1) {
            // Return first 200 characters if query not found
            return content.length() > 200 ? content.substring(0, 200) + "..." : content;
        }

        int start = Math.max(0, index - 100);
        int end = Math.min(content.length(), index + query.length() + 100);
        String snippet = content.substring(start, end);

        if (start > 0) snippet = "..." + snippet;
        if (end < content.length()) snippet = snippet + "...";

        return snippet;
    }
}

