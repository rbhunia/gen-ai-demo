package com.example.aspect;

import com.example.annotation.RateLimited;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingAspect {

    private final Map<String, Bucket> rateLimitBuckets;

    @Around("@annotation(rateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String service = rateLimited.service();
        Bucket bucket = rateLimitBuckets.get(service);

        if (bucket == null) {
            log.warn("No rate limit bucket found for service: {}", service);
            return joinPoint.proceed();
        }

        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for service: {}", service);
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletResponse response = attributes.getResponse();
                if (response != null) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setHeader("X-RateLimit-Limit", String.valueOf(bucket.getAvailableTokens()));
                    response.setHeader("Retry-After", "60");
                }
            }
            throw new RuntimeException("Rate limit exceeded for service: " + service);
        }

        return joinPoint.proceed();
    }
}

