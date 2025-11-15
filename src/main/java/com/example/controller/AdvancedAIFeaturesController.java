package com.example.controller;

import com.example.model.ChatMessage;
import com.example.model.dto.ChatRequest;
import com.example.model.dto.ChatResponse;
import com.example.model.dto.FraudAnalysisResult;
import com.example.model.dto.RiskAssessmentResult;
import com.example.model.dto.TransactionSummary;
import com.example.service.BankingToolService;
import com.example.service.ChatHistoryService;
import com.example.service.StructuredOutputService;
import com.example.service.StreamingChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Advanced AI Features Controller
 * Demonstrates additional Spring AI capabilities:
 * - Function Calling (Tool Calling)
 * - Structured Outputs
 * - Streaming Responses
 * - Chat History
 */
@RestController
@RequestMapping("/api/v1/ai/advanced")
@RequiredArgsConstructor
@Slf4j
public class AdvancedAIFeaturesController {

    private final BankingToolService bankingToolService;
    private final StructuredOutputService structuredOutputService;
    private final StreamingChatService streamingChatService;
    private final ChatHistoryService chatHistoryService;

    // ========== Function Calling / Tool Calling ==========

    @GetMapping("/tools/balance/{accountNumber}")
    public ResponseEntity<String> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(bankingToolService.getAccountBalance(accountNumber));
    }

    @GetMapping("/tools/transactions/{accountNumber}")
    public ResponseEntity<String> getTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(bankingToolService.getRecentTransactions(accountNumber, limit));
    }

    @GetMapping("/tools/status/{accountNumber}")
    public ResponseEntity<String> getAccountStatus(@PathVariable String accountNumber) {
        return ResponseEntity.ok(bankingToolService.checkAccountStatus(accountNumber));
    }

    @GetMapping("/tools/spending/{accountNumber}")
    public ResponseEntity<String> getSpendingByCategory(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(bankingToolService.calculateSpendingByCategory(accountNumber, days));
    }

    @GetMapping("/tools/summary/{accountNumber}")
    public ResponseEntity<String> getAccountSummary(@PathVariable String accountNumber) {
        return ResponseEntity.ok(bankingToolService.getAccountSummary(accountNumber));
    }

    // ========== Structured Outputs ==========

    @PostMapping("/structured/fraud-analysis")
    public ResponseEntity<FraudAnalysisResult> analyzeFraudStructured(
            @RequestBody String transactionDetails) {
        return ResponseEntity.ok(
                structuredOutputService.analyzeFraudStructured(transactionDetails));
    }

    @GetMapping("/structured/transaction-summary/{accountNumber}")
    public ResponseEntity<TransactionSummary> getTransactionSummaryStructured(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(
                structuredOutputService.getTransactionSummaryStructured(accountNumber, days));
    }

    @PostMapping("/structured/risk-assessment")
    public ResponseEntity<RiskAssessmentResult> assessRiskStructured(
            @RequestParam String customerId,
            @RequestBody String transactionDetails) {
        return ResponseEntity.ok(
                structuredOutputService.assessRiskStructured(customerId, transactionDetails));
    }

    // ========== Streaming Responses ==========

    @PostMapping(value = "/streaming/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        return streamingChatService.streamChatResponse(request);
    }

    // ========== Chat History ==========

    @PostMapping("/history/chat")
    public ResponseEntity<ChatResponse> chatWithHistory(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatHistoryService.handleChatWithHistory(request));
    }

    @GetMapping("/history/{customerId}")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(chatHistoryService.getHistory(customerId, limit));
    }

    @DeleteMapping("/history/{customerId}")
    public ResponseEntity<Void> clearHistory(@PathVariable String customerId) {
        chatHistoryService.clearHistory(customerId);
        return ResponseEntity.noContent().build();
    }
}

