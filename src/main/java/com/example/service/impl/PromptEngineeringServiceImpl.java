package com.example.service.impl;

import com.example.model.dto.AdvancedPromptRequest;
import com.example.model.dto.AdvancedPromptResponse;
import com.example.service.PromptEngineeringService;
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
public class PromptEngineeringServiceImpl implements PromptEngineeringService {

    private final ChatClient chatClient;

    @Override
    public AdvancedPromptResponse generatePrompt(AdvancedPromptRequest request) {
        log.info("Generating advanced prompt with technique: {}", request.getTechnique());

        String optimizedPrompt = switch (request.getTechnique() != null ? request.getTechnique().toUpperCase() : "ZERO_SHOT") {
            case "CHAIN_OF_THOUGHT" -> applyChainOfThought(request.getBasePrompt());
            case "FEW_SHOT" -> applyFewShotLearning(request.getBasePrompt(), 
                    request.getExamples() != null ? String.join("\n", request.getExamples()) : "");
            case "ROLE_BASED" -> applyRoleBasedPrompting(
                    request.getRole() != null ? request.getRole() : "expert",
                    request.getBasePrompt());
            default -> optimizePrompt(request.getBasePrompt(), 
                    request.getOptimizationGoal() != null ? request.getOptimizationGoal() : "CLARITY");
        };

        return AdvancedPromptResponse.builder()
                .optimizedPrompt(optimizedPrompt)
                .technique(request.getTechnique())
                .explanation("Prompt optimized using " + request.getTechnique() + " technique")
                .metadata(Map.of("originalLength", request.getBasePrompt().length(),
                        "optimizedLength", optimizedPrompt.length()))
                .estimatedTokens(estimateTokens(optimizedPrompt))
                .build();
    }

    @Override
    public String applyChainOfThought(String prompt) {
        return String.format("""
                Let's think step by step.
                
                %s
                
                Please break down your reasoning into clear steps:
                1. First, analyze the problem
                2. Then, identify key factors
                3. Next, apply relevant knowledge
                4. Finally, provide your conclusion
                """, prompt);
    }

    @Override
    public String applyFewShotLearning(String prompt, String examples) {
        return String.format("""
                Here are some examples:
                
                %s
                
                Now, based on these examples, please answer:
                
                %s
                """, examples, prompt);
    }

    @Override
    public String applyRoleBasedPrompting(String role, String task) {
        Map<String, String> roleTemplates = Map.of(
                "expert", "You are an expert in your field with years of experience.",
                "analyst", "You are a data analyst specializing in detailed analysis and insights.",
                "consultant", "You are a professional consultant providing strategic advice.",
                "educator", "You are an educator explaining concepts clearly and comprehensively."
        );

        String roleContext = roleTemplates.getOrDefault(role.toLowerCase(), 
                String.format("You are a %s with specialized knowledge.", role));

        return String.format("""
                %s
                
                Task: %s
                
                Please provide a detailed, professional response based on your expertise.
                """, roleContext, task);
    }

    @Override
    public String optimizePrompt(String prompt, String optimizationGoal) {
        String optimizationPrompt = switch (optimizationGoal.toUpperCase()) {
            case "CLARITY" -> """
                    Optimize the following prompt for maximum clarity and precision.
                    Make it unambiguous and easy to understand.
                    
                    Original prompt:
                    %s
                    """;
            case "BREVITY" -> """
                    Optimize the following prompt to be concise while maintaining all essential information.
                    Remove redundancy and unnecessary words.
                    
                    Original prompt:
                    %s
                    """;
            case "ACCURACY" -> """
                    Optimize the following prompt to improve accuracy of responses.
                    Add specific instructions and constraints.
                    
                    Original prompt:
                    %s
                    """;
            default -> """
                    Optimize the following prompt for better results.
                    
                    Original prompt:
                    %s
                    """;
        };

        PromptTemplate template = new PromptTemplate(String.format(optimizationPrompt, prompt));
        Prompt optimized = template.create(Collections.emptyMap());
        return chatClient.prompt(optimized).call().content();
    }

    private int estimateTokens(String text) {
        // Rough estimation: ~4 characters per token
        return text.length() / 4;
    }
}

