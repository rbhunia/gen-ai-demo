package com.example.service;

import com.example.model.Answer;
import com.example.model.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SpringAiBoardGameServiceTests {

    @Autowired
    private BoardGameService boardGameService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    private RelevancyEvaluator relevancyEvaluator;

    @BeforeEach
    public void setUp() {
        this.relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
    }

    @Test
    public void evaluateRelevancy() {
        String userText = "Why the sky is blue?";
        Question question = new Question(userText);
        Answer answer = boardGameService.askQuestion(question);

        EvaluationRequest evaluationRequest = new EvaluationRequest(userText, answer.answer());
        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);

        assertThat(evaluationResponse.isPass())
                .withFailMessage("""
                        ========================================
                        The answer "%s" is not considered relevant to the question "%s"
                        Please check your configuration and ensure that the model is capable of understanding and responding to the question
                        Note that the evaluation is based on the model's understanding and may not always align with human judgment.
                        For more information, refer to the Spring AI documentation: https://docs.spring.io/spring-ai/docs/current/reference/html/#chat-evaluation
                        ========================================
                        """, answer.answer(), userText)
                .isTrue();
    }
}