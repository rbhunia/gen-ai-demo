package com.example.service;

import java.util.List;

public interface RAGService {
    String retrieveRelevantContext(String query, int topK);
    List<String> retrieveRelevantContexts(String query, int topK);
}

