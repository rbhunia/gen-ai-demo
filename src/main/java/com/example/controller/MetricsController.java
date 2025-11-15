package com.example.controller;

import com.example.service.AIMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricsController {

    private final AIMetricsService aiMetricsService;

    @GetMapping("/ai")
    public ResponseEntity<Map<String, Object>> getAIMetrics() {
        Map<String, Object> metrics = aiMetricsService.getMetrics();
        return ResponseEntity.status(HttpStatus.OK).body(metrics);
    }
}

