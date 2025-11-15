package com.example.service.impl;

import com.example.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Override
    public List<Double> generateEmbedding(String text) {
        log.debug("Generating embedding for text: {}", text != null && text.length() > 50 ? text.substring(0, 50) : text);
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        
        // EmbeddingResponse.getResult().getOutput() returns float[] in Spring AI
        float[] embeddingArray = response.getResult().getOutput();
        List<Double> embedding = new java.util.ArrayList<>();
        for (float f : embeddingArray) {
            embedding.add((double) f);
        }
        return embedding;
    }

    @Override
    public List<List<Double>> generateEmbeddings(List<String> texts) {
        log.debug("Generating embeddings for {} texts", texts.size());
        EmbeddingResponse response = embeddingModel.embedForResponse(texts);
        List<List<Double>> embeddings = new java.util.ArrayList<>();
        for (var result : response.getResults()) {
            float[] embeddingArray = result.getOutput();
            List<Double> embedding = new java.util.ArrayList<>();
            for (float f : embeddingArray) {
                embedding.add((double) f);
            }
            embeddings.add(embedding);
        }
        return embeddings;
    }

    @Override
    public double cosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
        if (embedding1.size() != embedding2.size()) {
            throw new IllegalArgumentException("Embeddings must have the same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.size(); i++) {
            dotProduct += embedding1.get(i) * embedding2.get(i);
            norm1 += embedding1.get(i) * embedding1.get(i);
            norm2 += embedding2.get(i) * embedding2.get(i);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}

