package com.example.controller;

import com.example.model.dto.ChatRequest;
import com.example.model.dto.ChatResponse;
import com.example.service.CustomerServiceChatbot;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer-service")
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceController {
    
    private final CustomerServiceChatbot customerServiceChatbot;
    
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> handleInquiry(
            @RequestBody @Valid ChatRequest request) {
        log.info("Received customer service inquiry from customer: {}", request.getCustomerId());
        ChatResponse response = customerServiceChatbot.handleCustomerInquiry(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

