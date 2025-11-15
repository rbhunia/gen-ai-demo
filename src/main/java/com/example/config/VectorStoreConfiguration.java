package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Vector Store Configuration
 * 
 * Note: Spring AI auto-configures EmbeddingModel from application.properties
 * This configuration provides VectorStore beans when auto-configuration is not available
 */
@Configuration
@Slf4j
public class VectorStoreConfiguration {

    /**
     * SimpleVectorStore for development (in-memory)
     * Spring AI auto-configures EmbeddingModel from properties
     */
    @Bean
    @Primary
    @Profile("!kubernetes")
    @ConditionalOnMissingBean(name = "vectorStore")
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        log.info("Configuring SimpleVectorStore (in-memory) for development");
        return new SimpleVectorStore(embeddingModel);
    }

    /**
     * For production with PostgreSQL, PgVectorStore should be auto-configured
     * by spring-ai-pgvector-store-spring-boot-starter when:
     * - PostgreSQL datasource is available
     * - spring.ai.vectorstore.type=pgvector is set
     * 
     * If auto-configuration doesn't work, uncomment and configure manually:
     */
    /*
    @Bean
    @Primary
    @Profile("kubernetes")
    public VectorStore pgVectorStore(EmbeddingModel embeddingModel, DataSource dataSource) {
        log.info("Using PgVectorStore for production");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        // Configure PgVectorStore based on your Spring AI version
        // API may vary - check Spring AI documentation
        return new SimpleVectorStore(embeddingModel); // Fallback for now
    }
    */
}

