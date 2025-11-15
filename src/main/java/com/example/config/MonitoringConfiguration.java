package com.example.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MonitoringConfiguration {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        log.info("Configuring Micrometer TimedAspect for method-level timing");
        return new TimedAspect(registry);
    }
}

