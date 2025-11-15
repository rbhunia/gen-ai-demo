package com.example.controller;

import com.example.model.dto.ComplianceCheckRequest;
import com.example.model.dto.ComplianceCheckResponse;
import com.example.service.ComplianceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
@Slf4j
public class ComplianceController {
    
    private final ComplianceService complianceService;
    
    @PostMapping("/check")
    public ResponseEntity<ComplianceCheckResponse> checkCompliance(
            @RequestBody @Valid ComplianceCheckRequest request) {
        log.info("Received compliance check request: type={}, account={}", 
                request.getComplianceType(), request.getAccountNumber());
        ComplianceCheckResponse response = complianceService.checkCompliance(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

