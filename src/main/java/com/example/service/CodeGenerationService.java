package com.example.service;

import com.example.model.dto.CodeGenerationRequest;
import com.example.model.dto.CodeGenerationResponse;

public interface CodeGenerationService {
    CodeGenerationResponse generateCode(CodeGenerationRequest request);
    CodeGenerationResponse completeCode(String partialCode, String language);
    CodeGenerationResponse explainCode(String code, String language);
    CodeGenerationResponse refactorCode(String code, String language, String refactoringType);
}

