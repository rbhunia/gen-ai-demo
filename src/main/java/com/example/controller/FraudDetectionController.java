package com.example.controller;

import com.example.model.dto.FraudDetectionRequest;
import com.example.model.dto.FraudDetectionResponse;
import com.example.service.FraudDetectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fraud-detection")
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionController {
    
    private final FraudDetectionService fraudDetectionService;
    
    @PostMapping("/analyze")
    public ResponseEntity<FraudDetectionResponse> analyzeTransaction(
            @RequestBody @Valid FraudDetectionRequest request) {
        log.info("Received fraud detection request for account: {}", request.getAccountNumber());
        FraudDetectionResponse response = fraudDetectionService.detectFraud(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

