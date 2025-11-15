package com.example.service;

import java.util.List;

public interface EmbeddingService {
    List<Double> generateEmbedding(String text);
    List<List<Double>> generateEmbeddings(List<String> texts);
    double cosineSimilarity(List<Double> embedding1, List<Double> embedding2);
}

