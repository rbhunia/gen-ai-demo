package com.example.service;

import com.example.model.dto.DocumentSearchRequest;
import com.example.model.dto.DocumentSearchResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentSearchService {
    void indexDocument(String documentId, String content, String metadata);
    void indexDocumentFromFile(String documentId, MultipartFile file);
    DocumentSearchResponse searchDocuments(DocumentSearchRequest request);
    List<DocumentSearchResponse.DocumentResult> semanticSearch(String query, int topK);
    void deleteDocument(String documentId);
}

