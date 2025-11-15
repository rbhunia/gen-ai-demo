package com.example.controller;

import com.example.model.dto.TransactionAnalysisRequest;
import com.example.model.dto.TransactionAnalysisResponse;
import com.example.service.TransactionAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transaction-analysis")
@RequiredArgsConstructor
@Slf4j
public class TransactionAnalysisController {
    
    private final TransactionAnalysisService transactionAnalysisService;
    
    @PostMapping("/analyze")
    public ResponseEntity<TransactionAnalysisResponse> analyzeTransactions(
            @RequestBody @Valid TransactionAnalysisRequest request) {
        log.info("Received transaction analysis request for account: {}", request.getAccountNumber());
        TransactionAnalysisResponse response = transactionAnalysisService.analyzeTransactions(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

