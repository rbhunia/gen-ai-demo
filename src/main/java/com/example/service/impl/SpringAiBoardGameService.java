package com.example.service.impl;

import com.example.model.Answer;
import com.example.model.Question;
import com.example.service.BoardGameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpringAiBoardGameService implements BoardGameService {

    private final ChatClient chatClient;

    public SpringAiBoardGameService(ChatClient.Builder chatClientBuilder) {
        ChatOptions chatOptions = ChatOptions.builder()
                .model("llama3.2")
                .build();
        this.chatClient = chatClientBuilder.defaultOptions(chatOptions).build();
    }

    @Override
    public Answer askQuestion(Question question) {
        String answerText = chatClient.prompt().user(question.question())
                .call()
                .content();
        return new Answer(answerText);
    }
}
