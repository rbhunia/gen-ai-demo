package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@Slf4j
@EnableRetry
public class ChatClientConfiguration {

    @Bean
    @Primary
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        try {
            return ChatClient
                    .builder(ollamaChatModel)
                    .defaultSystem("You are an expert AI assistant specialized in banking, finance, fraud detection, risk assessment, and regulatory compliance. " +
                            "You provide accurate, professional, and helpful responses based on banking industry best practices.")
                    .build();
        } catch (Exception e) {
            log.error("Failed to initialize ChatClient", e);
            throw new BeanCreationException("ChatClient initialization failed", e);
        }
    }
}
