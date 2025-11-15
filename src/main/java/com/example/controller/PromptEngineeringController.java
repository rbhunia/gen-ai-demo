package com.example.controller;

import com.example.model.dto.AdvancedPromptRequest;
import com.example.model.dto.AdvancedPromptResponse;
import com.example.service.PromptEngineeringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/prompts")
@RequiredArgsConstructor
@Slf4j
public class PromptEngineeringController {

    private final PromptEngineeringService promptEngineeringService;

    @PostMapping("/optimize")
    public ResponseEntity<AdvancedPromptResponse> optimizePrompt(
            @RequestBody @Valid AdvancedPromptRequest request) {
        log.info("Prompt optimization request: technique={}", request.getTechnique());
        AdvancedPromptResponse response = promptEngineeringService.generatePrompt(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/chain-of-thought")
    public ResponseEntity<Map<String, String>> applyChainOfThought(
            @RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String optimized = promptEngineeringService.applyChainOfThought(prompt);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("original", prompt, "optimized", optimized));
    }

    @PostMapping("/few-shot")
    public ResponseEntity<Map<String, String>> applyFewShot(
            @RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String examples = request.get("examples");
        String optimized = promptEngineeringService.applyFewShotLearning(prompt, examples);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("original", prompt, "optimized", optimized));
    }

    @PostMapping("/role-based")
    public ResponseEntity<Map<String, String>> applyRoleBased(
            @RequestBody Map<String, String> request) {
        String role = request.get("role");
        String task = request.get("task");
        String optimized = promptEngineeringService.applyRoleBasedPrompting(role, task);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("role", role, "task", task, "optimized", optimized));
    }
}

