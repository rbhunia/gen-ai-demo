package com.example.service;

import java.util.Map;

public interface AIMetricsService {
    void recordAICall(String serviceName, String operation, long durationMs, boolean success);
    void recordTokenUsage(String serviceName, int inputTokens, int outputTokens);
    void recordCost(String serviceName, double cost);
    Map<String, Object> getMetrics();
}

