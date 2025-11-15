package com.example.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class RateLimitingConfiguration {

    // Rate limits per service (requests per minute)
    private static final Map<String, Integer> RATE_LIMITS = Map.of(
            "fraud-detection", 100,
            "transaction-analysis", 200,
            "customer-service", 500,
            "risk-assessment", 50,
            "compliance", 50,
            "code-generation", 30,
            "document-search", 100,
            "recommendation", 200
    );

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        Map<String, Bucket> buckets = new ConcurrentHashMap<>();
        
        RATE_LIMITS.forEach((service, limit) -> {
            Bandwidth bandwidth = Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(1)));
            Bucket bucket = Bucket.builder()
                    .addLimit(bandwidth)
                    .build();
            buckets.put(service, bucket);
            log.info("Configured rate limit for {}: {} requests/minute", service, limit);
        });

        return buckets;
    }
}

