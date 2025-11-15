package com.example.service;

import com.example.model.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Streaming Chat Service
 * Demonstrates Spring AI's streaming response capability
 * for real-time chat experiences
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingChatService {

    private final ChatClient chatClient;
    // Note: Streaming requires StreamingChatModel
    // For Ollama, this may need additional configuration

    /**
     * Stream chat response using Server-Sent Events (SSE)
     * This provides real-time streaming of AI responses
     */
    public SseEmitter streamChatResponse(ChatRequest request) {
        log.info("Streaming chat response for customer: {}", request.getCustomerId());
        
        SseEmitter emitter = new SseEmitter(30000L); // 30 second timeout

        CompletableFuture.runAsync(() -> {
            try {
                String prompt = buildPrompt(request);
                
                // Stream the response
                // Note: This requires StreamingChatModel support
                // For now, we'll simulate streaming by chunking the response
                String fullResponse = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();

                // Simulate streaming by sending chunks
                streamResponseChunks(emitter, fullResponse);
                
                emitter.complete();
            } catch (Exception e) {
                log.error("Error streaming chat response", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("Error: " + e.getMessage()));
                    emitter.completeWithError(e);
                } catch (IOException ioException) {
                    log.error("Error sending error event", ioException);
                }
            }
        });

        return emitter;
    }

    /**
     * Stream response in chunks to simulate real streaming
     * In production with proper StreamingChatModel, this would be automatic
     */
    private void streamResponseChunks(SseEmitter emitter, String response) throws IOException {
        String[] words = response.split(" ");
        for (int i = 0; i < words.length; i++) {
            String chunk = words[i] + (i < words.length - 1 ? " " : "");
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(chunk));
            
            // Small delay to simulate streaming
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private String buildPrompt(ChatRequest request) {
        return String.format("""
                You are a helpful banking assistant.
                Customer ID: %s
                Message: %s
                Context: %s
                
                Provide a helpful and professional response.
                """,
                request.getCustomerId(),
                request.getMessage(),
                request.getContext() != null ? request.getContext() : "No additional context");
    }
}

