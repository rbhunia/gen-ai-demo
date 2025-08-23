package com.example.service;

import com.example.model.Answer;
import com.example.model.Question;

public interface BoardGameService {

    Answer askQuestion(Question question);
}
