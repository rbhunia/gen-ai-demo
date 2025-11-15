package com.example.service.impl;

import com.example.model.dto.CodeGenerationRequest;
import com.example.model.dto.CodeGenerationResponse;
import com.example.service.CodeGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeGenerationServiceImpl implements CodeGenerationService {

    private final ChatClient chatClient;

    @Override
    public CodeGenerationResponse generateCode(CodeGenerationRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Generating code: language={}, description={}", request.getLanguage(), request.getDescription());

        String promptTemplate = """
                You are an expert {language} developer.
                Generate clean, production-ready code based on the following requirements.
                
                Requirements:
                {description}
                
                {frameworkContext}
                {styleContext}
                {additionalContext}
                
                Please provide:
                1. Complete, working code
                2. Brief explanation of the implementation
                3. Best practices used
                4. Any important considerations
                
                Format your response as:
                CODE:
                [your code here]
                
                EXPLANATION:
                [explanation here]
                
                SUGGESTIONS:
                [suggestions, one per line]
                """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("language", request.getLanguage());
        variables.put("description", request.getDescription());
        variables.put("frameworkContext", request.getFramework() != null ?
                String.format("Framework: %s\n", request.getFramework()) : "");
        variables.put("styleContext", request.getStyle() != null ?
                String.format("Code Style: %s\n", request.getStyle()) : "");
        variables.put("additionalContext", request.getContext() != null ?
                String.format("Additional Context: %s\n", request.getContext()) : "");

        PromptTemplate template = new PromptTemplate(promptTemplate);
        Prompt prompt = template.create(variables);
        String aiResponse = chatClient.prompt(prompt).call().content();

        CodeGenerationResponse response = parseCodeResponse(aiResponse, request.getLanguage());
        response.setGenerationTimeMs(System.currentTimeMillis() - startTime);

        return response;
    }

    @Override
    public CodeGenerationResponse completeCode(String partialCode, String language) {
        long startTime = System.currentTimeMillis();
        log.info("Completing code: language={}", language);

        String promptTemplate = """
                You are an expert {language} developer.
                Complete the following partial code. Provide only the completion, maintaining the existing code style.
                
                Partial Code:
                {partialCode}
                
                Provide the completed code with a brief explanation.
                """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("language", language);
        variables.put("partialCode", partialCode);

        PromptTemplate template = new PromptTemplate(promptTemplate);
        Prompt prompt = template.create(variables);
        String aiResponse = chatClient.prompt(prompt).call().content();

        CodeGenerationResponse response = parseCodeResponse(aiResponse, language);
        response.setGenerationTimeMs(System.currentTimeMillis() - startTime);

        return response;
    }

    @Override
    public CodeGenerationResponse explainCode(String code, String language) {
        long startTime = System.currentTimeMillis();
        log.info("Explaining code: language={}", language);

        String promptTemplate = """
                You are an expert {language} developer and educator.
                Explain the following code in detail, including:
                1. What the code does
                2. How it works
                3. Key concepts and patterns used
                4. Potential improvements or considerations
                
                Code:
                {code}
                """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("language", language);
        variables.put("code", code);

        PromptTemplate template = new PromptTemplate(promptTemplate);
        Prompt prompt = template.create(variables);
        String aiResponse = chatClient.prompt(prompt).call().content();

        return CodeGenerationResponse.builder()
                .generatedCode(code)
                .language(language)
                .explanation(aiResponse)
                .suggestions(extractSuggestions(aiResponse))
                .metadata(Map.of("type", "explanation"))
                .generationTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }

    @Override
    public CodeGenerationResponse refactorCode(String code, String language, String refactoringType) {
        long startTime = System.currentTimeMillis();
        log.info("Refactoring code: language={}, type={}", language, refactoringType);

        String promptTemplate = """
                You are an expert {language} developer specializing in code refactoring.
                Refactor the following code with focus on: {refactoringType}
                
                Original Code:
                {code}
                
                Provide:
                1. Refactored code
                2. Explanation of changes
                3. Benefits of the refactoring
                4. Any trade-offs or considerations
                """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("language", language);
        variables.put("refactoringType", refactoringType);
        variables.put("code", code);

        PromptTemplate template = new PromptTemplate(promptTemplate);
        Prompt prompt = template.create(variables);
        String aiResponse = chatClient.prompt(prompt).call().content();

        CodeGenerationResponse response = parseCodeResponse(aiResponse, language);
        response.setGenerationTimeMs(System.currentTimeMillis() - startTime);
        response.getMetadata().put("refactoringType", refactoringType);

        return response;
    }

    private CodeGenerationResponse parseCodeResponse(String aiResponse, String language) {
        String code = extractSection(aiResponse, "CODE:");
        String explanation = extractSection(aiResponse, "EXPLANATION:");
        List<String> suggestions = extractSuggestions(aiResponse);

        return CodeGenerationResponse.builder()
                .generatedCode(code != null ? code : aiResponse)
                .language(language)
                .explanation(explanation != null ? explanation : "Code generated successfully")
                .suggestions(suggestions)
                .metadata(new HashMap<>())
                .build();
    }

    private String extractSection(String text, String sectionName) {
        int startIndex = text.indexOf(sectionName);
        if (startIndex == -1) {
            return null;
        }

        startIndex += sectionName.length();
        int endIndex = text.indexOf("\n\n", startIndex);
        if (endIndex == -1) {
            endIndex = text.length();
        }

        return text.substring(startIndex, endIndex).trim();
    }

    private List<String> extractSuggestions(String text) {
        List<String> suggestions = new ArrayList<>();
        int startIndex = text.indexOf("SUGGESTIONS:");
        if (startIndex == -1) {
            return suggestions;
        }

        String suggestionsText = text.substring(startIndex + "SUGGESTIONS:".length()).trim();
        String[] lines = suggestionsText.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("-") || line.matches("\\d+\\.")) {
                line = line.replaceFirst("^[-\\d.]\\s*", "").trim();
            }
            if (!line.isEmpty()) {
                suggestions.add(line);
            }
        }

        return suggestions;
    }
}

