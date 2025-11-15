package com.example.service;

import com.example.model.dto.ComplianceCheckRequest;
import com.example.model.dto.ComplianceCheckResponse;

public interface ComplianceService {
    ComplianceCheckResponse checkCompliance(ComplianceCheckRequest request);
}

