package com.example.service.impl;

import com.example.service.AIMetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIMetricsServiceImpl implements AIMetricsService {

    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicLong> tokenCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> costCounters = new ConcurrentHashMap<>();

    @Override
    public void recordAICall(String serviceName, String operation, long durationMs, boolean success) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("ai.call.duration")
                .tag("service", serviceName)
                .tag("operation", operation)
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry));

        Counter.builder("ai.call.count")
                .tag("service", serviceName)
                .tag("operation", operation)
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry)
                .increment();

        log.debug("Recorded AI call: service={}, operation={}, duration={}ms, success={}",
                serviceName, operation, durationMs, success);
    }

    @Override
    public void recordTokenUsage(String serviceName, int inputTokens, int outputTokens) {
        meterRegistry.counter("ai.tokens.input", "service", serviceName).increment(inputTokens);
        meterRegistry.counter("ai.tokens.output", "service", serviceName).increment(outputTokens);
        meterRegistry.counter("ai.tokens.total", "service", serviceName).increment(inputTokens + outputTokens);

        tokenCounters.computeIfAbsent(serviceName + ".input", k -> new AtomicLong(0)).addAndGet(inputTokens);
        tokenCounters.computeIfAbsent(serviceName + ".output", k -> new AtomicLong(0)).addAndGet(outputTokens);

        log.debug("Recorded token usage: service={}, input={}, output={}", serviceName, inputTokens, outputTokens);
    }

    @Override
    public void recordCost(String serviceName, double cost) {
        meterRegistry.counter("ai.cost.total", "service", serviceName).increment(cost);
        costCounters.computeIfAbsent(serviceName, k -> new AtomicLong(0))
                .addAndGet((long) (cost * 1000)); // Store in millicents for precision

        log.debug("Recorded cost: service={}, cost=${}", serviceName, cost);
    }

    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Token usage
        Map<String, Long> tokenUsage = new HashMap<>();
        tokenCounters.forEach((key, value) -> tokenUsage.put(key, value.get()));
        metrics.put("tokenUsage", tokenUsage);

        // Cost tracking
        Map<String, Double> costs = new HashMap<>();
        costCounters.forEach((key, value) -> costs.put(key, value.get() / 1000.0));
        metrics.put("costs", costs);

        return metrics;
    }
}

