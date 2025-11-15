package com.example.controller;

import com.example.model.dto.RiskAssessmentRequest;
import com.example.model.dto.RiskAssessmentResponse;
import com.example.service.RiskAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risk-assessment")
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentController {
    
    private final RiskAssessmentService riskAssessmentService;
    
    @PostMapping("/assess")
    public ResponseEntity<RiskAssessmentResponse> assessRisk(
            @RequestBody @Valid RiskAssessmentRequest request) {
        log.info("Received risk assessment request for account: {}", request.getAccountNumber());
        RiskAssessmentResponse response = riskAssessmentService.assessRisk(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

