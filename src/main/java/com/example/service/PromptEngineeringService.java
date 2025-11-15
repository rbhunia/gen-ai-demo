package com.example.service;

import com.example.model.dto.AdvancedPromptRequest;
import com.example.model.dto.AdvancedPromptResponse;

public interface PromptEngineeringService {
    AdvancedPromptResponse generatePrompt(AdvancedPromptRequest request);
    String applyChainOfThought(String prompt);
    String applyFewShotLearning(String prompt, String examples);
    String applyRoleBasedPrompting(String role, String task);
    String optimizePrompt(String prompt, String optimizationGoal);
}

