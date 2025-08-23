package com.example.service.impl;

import com.example.model.Answer;
import com.example.model.Question;
import com.example.service.BoardGameService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class SpringAiBoardGameService implements BoardGameService {

    private final ChatClient chatClient;

    @Override
    public Answer askQuestion(Question question) {
        String answerText = chatClient.prompt()
                .user(question.question())
                .call()
                .content();
        return new Answer(answerText);
    }
}
