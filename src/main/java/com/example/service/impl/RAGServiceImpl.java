package com.example.service.impl;

import com.example.service.BankingKnowledgeService;
import com.example.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RAGServiceImpl implements RAGService {

    private final BankingKnowledgeService bankingKnowledgeService;

    @Override
    public String retrieveRelevantContext(String query, int topK) {
        log.debug("Retrieving relevant context for query: {}", query);
        List<Document> documents = bankingKnowledgeService.searchSimilar(query, topK);
        
        if (documents.isEmpty()) {
            return "No relevant banking knowledge found for the query.";
        }

        StringBuilder context = new StringBuilder("Relevant Banking Knowledge:\n\n");
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            context.append(String.format("[%d] %s\n\n", i + 1, doc.getContent()));
        }

        return context.toString();
    }

    @Override
    public List<String> retrieveRelevantContexts(String query, int topK) {
        log.debug("Retrieving relevant contexts for query: {}", query);
        List<Document> documents = bankingKnowledgeService.searchSimilar(query, topK);
        return documents.stream()
                .map(Document::getContent)
                .collect(Collectors.toList());
    }
}

