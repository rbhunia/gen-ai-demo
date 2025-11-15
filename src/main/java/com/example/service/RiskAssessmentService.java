package com.example.service;

import com.example.model.dto.RiskAssessmentRequest;
import com.example.model.dto.RiskAssessmentResponse;

public interface RiskAssessmentService {
    RiskAssessmentResponse assessRisk(RiskAssessmentRequest request);
}

