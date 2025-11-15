package com.example.service;

import com.example.model.ChatMessage;
import com.example.model.dto.ChatRequest;
import com.example.model.dto.ChatResponse;
import com.example.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chat History Service
 * Demonstrates maintaining conversation context using Spring AI's message history
 * This enables context-aware conversations across multiple interactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryService {

    private final ChatClient chatClient;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Handle chat with conversation history
     * Maintains context across multiple interactions
     */
    public ChatResponse handleChatWithHistory(ChatRequest request) {
        log.info("Handling chat with history for customer: {}", request.getCustomerId());

        // Retrieve conversation history
        List<ChatMessage> history = chatMessageRepository
                .findByCustomerIdOrderByTimestampDesc(request.getCustomerId())
                .stream()
                .limit(10) // Last 10 messages for context
                .collect(Collectors.toList());

        // Build message history for AI context
        List<Message> messages = buildMessageHistory(history, request);

        // Get AI response with context
        String response = chatClient.prompt()
                .messages(messages)
                .call()
                .content();

        // Save conversation to history
        saveToHistory(request.getCustomerId(), request.getMessage(), response);

        return ChatResponse.builder()
                .response(response)
                .customerId(request.getCustomerId())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Build message history from stored conversations
     */
    private List<Message> buildMessageHistory(List<ChatMessage> history, ChatRequest currentRequest) {
        List<Message> messages = new ArrayList<>();

        // Add historical messages in chronological order
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatMessage msg = history.get(i);
            if ("USER".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("ASSISTANT".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // Add current user message
        messages.add(new UserMessage(currentRequest.getMessage()));

        return messages;
    }

    /**
     * Save conversation to history
     */
    private void saveToHistory(String customerId, String userMessage, String assistantResponse) {
        // Save user message
        ChatMessage userMsg = ChatMessage.builder()
                .customerId(customerId)
                .role("USER")
                .content(userMessage)
                .timestamp(LocalDateTime.now())
                .build();
        chatMessageRepository.save(userMsg);

        // Save assistant response
        ChatMessage assistantMsg = ChatMessage.builder()
                .customerId(customerId)
                .role("ASSISTANT")
                .content(assistantResponse)
                .timestamp(LocalDateTime.now())
                .build();
        chatMessageRepository.save(assistantMsg);
    }

    /**
     * Clear conversation history for a customer
     */
    public void clearHistory(String customerId) {
        log.info("Clearing chat history for customer: {}", customerId);
        chatMessageRepository.deleteByCustomerId(customerId);
    }

    /**
     * Get conversation history
     */
    public List<ChatMessage> getHistory(String customerId, int limit) {
        return chatMessageRepository
                .findByCustomerIdOrderByTimestampDesc(customerId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}

