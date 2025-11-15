package com.example.service;

import com.example.model.dto.TransactionAnalysisRequest;
import com.example.model.dto.TransactionAnalysisResponse;

public interface TransactionAnalysisService {
    TransactionAnalysisResponse analyzeTransactions(TransactionAnalysisRequest request);
}

