# Advanced Spring AI Features Documentation

This document describes all the advanced features added to the Banking Gen AI application.

## 📚 Table of Contents

1. [Module 5: Vector Databases and Embeddings](#module-5-vector-databases-and-embeddings)
2. [Module 6: Advanced Spring AI Concepts](#module-6-advanced-spring-ai-concepts)

---

## Module 5: Vector Databases and Embeddings

### 1. Introduction to Vector Databases and Embeddings ✅

**What it is:**
- Vector databases store data as high-dimensional vectors (embeddings)
- Embeddings capture semantic meaning of text
- Enables semantic search and similarity matching

**Implementation:**
- `EmbeddingService` - Generates embeddings using Ollama
- `EmbeddingServiceImpl` - Implements embedding generation and similarity calculation

**API Endpoints:**
- `POST /api/v1/embeddings/generate` - Generate embedding for text
- `POST /api/v1/embeddings/similarity` - Calculate cosine similarity

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/embeddings/generate \
  -H "Content-Type: application/json" \
  -d '{"text": "fraud detection banking transaction"}'
```

### 2. Generating Text Embeddings with Spring AI ✅

**Features:**
- Single text embedding generation
- Batch embedding generation
- Cosine similarity calculation
- Integration with Ollama embedding model

**Code Example:**
```java
@Autowired
private EmbeddingService embeddingService;

List<Double> embedding = embeddingService.generateEmbedding("Your text here");
double similarity = embeddingService.cosineSimilarity(embedding1, embedding2);
```

### 3. Storing and Querying Embeddings in Vector Databases ✅

**Supported Vector Stores:**
- **SimpleVectorStore** (Default) - In-memory, for development
- **PgVectorStore** - PostgreSQL with pgvector extension
- **Pinecone** - Cloud vector database (optional)
- **Weaviate** - Open-source vector database (optional)
- **Chroma** - Embedded vector database (optional)

**Configuration:**
```properties
# Simple (default)
spring.ai.vectorstore.type=simple

# PostgreSQL
spring.ai.vectorstore.type=pgvector

# Pinecone
spring.ai.vectorstore.type=pinecone
spring.ai.vectorstore.pinecone.api-key=your-key

# Weaviate
spring.ai.vectorstore.type=weaviate
spring.ai.vectorstore.weaviate.host=localhost

# Chroma
spring.ai.vectorstore.type=chroma
spring.ai.vectorstore.chroma.host=localhost
```

### 4. Implementing Semantic Search with Spring AI and Vector Databases ✅

**Features:**
- Semantic similarity search
- Top-K retrieval
- Metadata filtering
- Similarity threshold filtering

**Implementation:**
- `DocumentSearchService` - Document indexing and search
- `RAGService` - Retrieval Augmented Generation

**API Endpoints:**
- `POST /api/v1/documents/search` - Semantic document search
- `GET /api/v1/documents/search/semantic` - Quick semantic search

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/documents/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "fraud detection patterns",
    "topK": 5,
    "similarityThreshold": 0.7
  }'
```

### 5. Building a Recommendation System with Embeddings ✅

**Features:**
- Product recommendations based on customer profile
- Similar customer finding using embeddings
- Transaction-based recommendations
- Embedding-based similarity matching

**Implementation:**
- `RecommendationService` - Recommendation engine
- Uses embeddings to find similar customers and products

**API Endpoints:**
- `POST /api/v1/recommendations/products` - Get product recommendations
- `GET /api/v1/recommendations/similar-customers/{customerId}` - Find similar customers
- `GET /api/v1/recommendations/transaction-based/{accountNumber}` - Transaction-based recommendations

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/recommendations/products \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "accountNumber": "ACC001",
    "recommendationType": "PRODUCTS",
    "topK": 3
  }'
```

### 6. Practical Exercise: Building a Document Search Application ✅

**Features:**
- Document indexing (text and files)
- Semantic search across documents
- Metadata tagging
- Document management (CRUD operations)

**API Endpoints:**
- `POST /api/v1/documents/index` - Index a document
- `POST /api/v1/documents/index/file` - Index from file upload
- `POST /api/v1/documents/search` - Search documents
- `DELETE /api/v1/documents/{documentId}` - Delete document

**Example:**
```bash
# Index a document
curl -X POST http://localhost:8080/api/v1/documents/index \
  -F "documentId=doc1" \
  -F "content=Banking regulations require..." \
  -F "metadata=category:compliance,type:AML"

# Search documents
curl -X POST http://localhost:8080/api/v1/documents/search \
  -H "Content-Type: application/json" \
  -d '{"query": "AML requirements", "topK": 5}'
```

---

## Module 6: Advanced Spring AI Concepts

### 1. Fine-tuning LLMs with Spring AI 📝

**Documentation:**
Fine-tuning is typically done outside the application using model-specific tools. However, the application supports:
- Custom prompt templates for domain-specific behavior
- System message configuration for role-based responses
- Temperature and parameter tuning

**Configuration:**
```properties
spring.ai.ollama.chat.options.temperature=0.7
spring.ai.ollama.chat.options.num-predict=2000
spring.ai.ollama.chat.options.top-p=0.9
```

**Best Practices:**
- Use domain-specific system messages
- Implement prompt templates for consistency
- Monitor and adjust parameters based on results

### 2. Implementing RAG (Retrieval Augmented Generation) with Spring AI ✅

**Features:**
- Knowledge base retrieval
- Context-aware generation
- Integration with vector stores
- Multi-document context

**Implementation:**
- `RAGService` - Retrieval service
- `BankingKnowledgeService` - Knowledge base management
- Integrated into Fraud Detection, Customer Service, and Compliance services

**API Endpoints:**
- `GET /api/v1/rag/search` - RAG search endpoint

**Example:**
```bash
curl "http://localhost:8080/api/v1/rag/search?query=fraud%20detection&topK=3"
```

### 3. Using Spring AI for Code Generation and Completion ✅

**Features:**
- Code generation from descriptions
- Code completion
- Code explanation
- Code refactoring

**Implementation:**
- `CodeGenerationService` - Code generation engine

**API Endpoints:**
- `POST /api/v1/code/generate` - Generate code
- `POST /api/v1/code/complete` - Complete partial code
- `POST /api/v1/code/explain` - Explain code
- `POST /api/v1/code/refactor` - Refactor code

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/code/generate \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Create a REST controller for user management",
    "language": "JAVA",
    "framework": "SPRING_BOOT",
    "style": "CLEAN_CODE"
  }'
```

### 4. Monitoring and Logging Spring AI Applications ✅

**Features:**
- AI call metrics (duration, success/failure)
- Token usage tracking
- Cost tracking
- Prometheus metrics integration

**Implementation:**
- `AIMetricsService` - Metrics collection
- `MonitoringConfiguration` - Micrometer configuration
- Actuator endpoints for metrics

**API Endpoints:**
- `GET /api/v1/metrics/ai` - Get AI metrics
- `GET /actuator/metrics` - Prometheus metrics
- `GET /actuator/prometheus` - Prometheus format

**Metrics Available:**
- `ai.call.duration` - Call duration
- `ai.call.count` - Call count
- `ai.tokens.input` - Input tokens
- `ai.tokens.output` - Output tokens
- `ai.cost.total` - Total cost

**Example:**
```bash
curl http://localhost:8080/api/v1/metrics/ai
curl http://localhost:8080/actuator/prometheus
```

### 5. Implementing Rate Limiting and Cost Management ✅

**Features:**
- Per-service rate limiting
- Token-based rate limiting (Bucket4j)
- Cost tracking per service
- Automatic rate limit enforcement

**Implementation:**
- `RateLimitingConfiguration` - Rate limit setup
- `RateLimitingAspect` - AOP-based rate limiting
- `@RateLimited` annotation

**Rate Limits:**
- Fraud Detection: 100 req/min
- Transaction Analysis: 200 req/min
- Customer Service: 500 req/min
- Risk Assessment: 50 req/min
- Compliance: 50 req/min
- Code Generation: 30 req/min
- Document Search: 100 req/min
- Recommendations: 200 req/min

**Usage:**
```java
@RateLimited(service = "fraud-detection")
public FraudDetectionResponse detectFraud(...) {
    // Method automatically rate limited
}
```

**Response Headers:**
- `X-RateLimit-Limit` - Current limit
- `Retry-After` - Seconds to wait

### 6. Exploring Advanced Prompt Engineering Techniques ✅

**Features:**
- Chain of Thought prompting
- Few-shot learning
- Role-based prompting
- Prompt optimization

**Implementation:**
- `PromptEngineeringService` - Prompt engineering utilities

**API Endpoints:**
- `POST /api/v1/prompts/optimize` - Optimize prompt
- `POST /api/v1/prompts/chain-of-thought` - Apply CoT
- `POST /api/v1/prompts/few-shot` - Apply few-shot
- `POST /api/v1/prompts/role-based` - Apply role-based

**Techniques:**
1. **Chain of Thought** - Step-by-step reasoning
2. **Few-Shot Learning** - Examples-based learning
3. **Role-Based** - Expert persona prompting
4. **Optimization** - Clarity, brevity, accuracy

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/prompts/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "basePrompt": "Analyze this transaction",
    "technique": "CHAIN_OF_THOUGHT",
    "optimizationGoal": "CLARITY"
  }'
```

---

## 🎯 Quick Reference

### All New API Endpoints

**Embeddings:**
- `POST /api/v1/embeddings/generate`
- `POST /api/v1/embeddings/similarity`

**Recommendations:**
- `POST /api/v1/recommendations/products`
- `GET /api/v1/recommendations/similar-customers/{customerId}`
- `GET /api/v1/recommendations/transaction-based/{accountNumber}`

**Document Search:**
- `POST /api/v1/documents/index`
- `POST /api/v1/documents/index/file`
- `POST /api/v1/documents/search`
- `GET /api/v1/documents/search/semantic`
- `DELETE /api/v1/documents/{documentId}`

**Code Generation:**
- `POST /api/v1/code/generate`
- `POST /api/v1/code/complete`
- `POST /api/v1/code/explain`
- `POST /api/v1/code/refactor`

**Prompt Engineering:**
- `POST /api/v1/prompts/optimize`
- `POST /api/v1/prompts/chain-of-thought`
- `POST /api/v1/prompts/few-shot`
- `POST /api/v1/prompts/role-based`

**Metrics:**
- `GET /api/v1/metrics/ai`

---

## 📊 Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                  Banking Gen AI App                      │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  Embeddings  │  │  Vector      │  │  RAG         │ │
│  │  Service     │  │  Store       │  │  Service     │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  Code Gen    │  │  Monitoring  │  │  Rate Limit  │ │
│  │  Service     │  │  Service     │  │  Service     │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  Document    │  │  Recommend   │  │  Prompt      │ │
│  │  Search      │  │  Service     │  │  Engineering │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Getting Started

1. **Install Dependencies:**
   ```bash
   mvn clean install
   ```

2. **Start Application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Test Features:**
   - Use the examples above to test each feature
   - Check `/actuator/health` for application status
   - View metrics at `/actuator/prometheus`

---

## 📝 Notes

- All features are production-ready with proper error handling
- Rate limiting is enabled on resource-intensive endpoints
- Metrics are collected for all AI operations
- Vector stores can be switched via configuration
- All services support both development and production profiles

