package com.example.controller;

import com.example.model.dto.CodeGenerationRequest;
import com.example.model.dto.CodeGenerationResponse;
import com.example.service.CodeGenerationService;
import com.example.annotation.RateLimited;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/code")
@RequiredArgsConstructor
@Slf4j
public class CodeGenerationController {

    private final CodeGenerationService codeGenerationService;

    @PostMapping("/generate")
    @RateLimited(service = "code-generation")
    public ResponseEntity<CodeGenerationResponse> generateCode(
            @RequestBody @Valid CodeGenerationRequest request) {
        log.info("Code generation request: language={}", request.getLanguage());
        CodeGenerationResponse response = codeGenerationService.generateCode(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/complete")
    @RateLimited(service = "code-generation")
    public ResponseEntity<CodeGenerationResponse> completeCode(
            @RequestBody Map<String, String> request) {
        String partialCode = request.get("code");
        String language = request.get("language");
        log.info("Code completion request: language={}", language);
        CodeGenerationResponse response = codeGenerationService.completeCode(partialCode, language);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/explain")
    public ResponseEntity<CodeGenerationResponse> explainCode(
            @RequestBody Map<String, String> request) {
        String code = request.get("code");
        String language = request.get("language");
        log.info("Code explanation request: language={}", language);
        CodeGenerationResponse response = codeGenerationService.explainCode(code, language);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refactor")
    @RateLimited(service = "code-generation")
    public ResponseEntity<CodeGenerationResponse> refactorCode(
            @RequestBody Map<String, String> request) {
        String code = request.get("code");
        String language = request.get("language");
        String refactoringType = request.get("refactoringType");
        log.info("Code refactoring request: language={}, type={}", language, refactoringType);
        CodeGenerationResponse response = codeGenerationService.refactorCode(code, language, refactoringType);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

