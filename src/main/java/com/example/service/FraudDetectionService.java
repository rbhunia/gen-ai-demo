package com.example.service;

import com.example.model.dto.FraudDetectionRequest;
import com.example.model.dto.FraudDetectionResponse;

public interface FraudDetectionService {
    FraudDetectionResponse detectFraud(FraudDetectionRequest request);
}

