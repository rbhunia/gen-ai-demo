package com.example.service;

import com.example.model.dto.ChatRequest;
import com.example.model.dto.ChatResponse;

public interface CustomerServiceChatbot {
    ChatResponse handleCustomerInquiry(ChatRequest request);
}

