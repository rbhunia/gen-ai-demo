package com.example.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

public interface BankingKnowledgeService {
    void initializeKnowledgeBase();
    void addDocument(String content, String metadata);
    List<Document> searchSimilar(String query, int topK);
}

