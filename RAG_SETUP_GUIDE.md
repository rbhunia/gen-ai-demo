# RAG (Retrieval Augmented Generation) Setup Guide

This guide explains how RAG and Vector Database are integrated into the Banking Gen AI application.

## 🎯 Overview

RAG (Retrieval Augmented Generation) enhances AI responses by retrieving relevant information from a knowledge base before generating answers. This ensures more accurate, context-aware, and up-to-date responses.

## 📦 Components Added

### 1. **Vector Store Configuration**
- **SimpleVectorStore**: In-memory vector store for development (H2)
- **PgVectorStore**: PostgreSQL-based vector store for production
- Automatic selection based on Spring profile

### 2. **Embedding Model**
- **Ollama Embeddings**: Uses `nomic-embed-text` model for generating embeddings
- Converts text documents into vector representations
- Enables semantic search capabilities

### 3. **Banking Knowledge Service**
- Populates vector store with banking domain knowledge
- 10 pre-loaded documents covering:
  - AML Regulations
  - KYC Requirements
  - Fraud Detection Best Practices
  - Risk Assessment Criteria
  - Transaction Monitoring Rules
  - Customer Service Policies
  - Compliance Types
  - Security Measures

### 4. **RAG Service**
- Retrieves relevant context from vector store
- Performs semantic similarity search
- Returns top-K most relevant documents

### 5. **RAG Integration**
- Integrated into:
  - Fraud Detection Service
  - Customer Service Chatbot
  - Compliance Service

## 🚀 Setup Instructions

### Step 1: Install Embedding Model

```bash
# Pull the embedding model in Ollama
ollama pull nomic-embed-text
```

### Step 2: Verify Ollama is Running

```bash
# Check if Ollama is running
curl http://localhost:11434/api/tags

# Should show both models:
# - llama3.2:3b (for chat)
# - nomic-embed-text (for embeddings)
```

### Step 3: Configure Application

The application is already configured in `application.properties`:

```properties
# Embeddings Configuration
spring.ai.ollama.embedding.model=nomic-embed-text
spring.ai.ollama.embedding.options.temperature=0.0

# Vector Store Configuration
spring.ai.vectorstore.type=simple  # For development
```

### Step 4: Run the Application

```bash
mvn spring-boot:run
```

The application will automatically:
1. Initialize the vector store
2. Load banking knowledge documents
3. Generate embeddings for all documents
4. Make them available for RAG queries

## 🔍 How RAG Works

### Flow Diagram

```
User Query
    ↓
RAG Service
    ↓
Vector Store (Semantic Search)
    ↓
Retrieve Top-K Relevant Documents
    ↓
Combine with User Query
    ↓
AI Model (with Context)
    ↓
Enhanced Response
```

### Example: Fraud Detection with RAG

1. **User Request**: Analyze transaction for fraud
2. **RAG Retrieval**: 
   - Query: "fraud detection transaction analysis DEBIT 50000 OTHER"
   - Retrieves: Fraud detection best practices, transaction monitoring rules, risk indicators
3. **AI Analysis**: 
   - Uses retrieved knowledge + transaction data
   - Generates more accurate fraud assessment
4. **Response**: Enhanced fraud detection with regulatory context

## 📊 Vector Store Options

### Option 1: SimpleVectorStore (Development)
- **Type**: In-memory
- **Pros**: Fast, no setup required
- **Cons**: Data lost on restart
- **Use Case**: Development, testing

```properties
spring.ai.vectorstore.type=simple
```

### Option 2: PgVectorStore (Production)
- **Type**: PostgreSQL with pgvector extension
- **Pros**: Persistent, scalable, production-ready
- **Cons**: Requires PostgreSQL setup
- **Use Case**: Production, Kubernetes

```properties
spring.ai.vectorstore.type=pgvector
spring.datasource.url=jdbc:postgresql://localhost:5432/bankingdb
```

## 🧪 Testing RAG

### Test RAG Search Endpoint

```bash
curl "http://localhost:8080/api/v1/rag/search?query=fraud%20detection&topK=3"
```

### Test Fraud Detection with RAG

```bash
curl -X POST http://localhost:8080/api/v1/fraud-detection/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "ACC001",
    "transactionType": "DEBIT",
    "amount": 50000.00,
    "currency": "USD",
    "merchantName": "Suspicious Merchant",
    "merchantCategory": "OTHER",
    "location": "Offshore",
    "transactionDate": "2024-01-15T10:30:00"
  }'
```

The response will now include context from the banking knowledge base!

### Test Customer Service with RAG

```bash
curl -X POST http://localhost:8080/api/v1/customer-service/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What are the AML reporting requirements?",
    "customerId": "CUST001"
  }'
```

## 📝 Adding Custom Knowledge

### Method 1: Programmatically

```java
@Autowired
private BankingKnowledgeService knowledgeService;

public void addCustomKnowledge() {
    knowledgeService.addDocument(
        "Your custom banking knowledge here...",
        "category:custom,type:policy"
    );
}
```

### Method 2: Update BankingKnowledgeServiceImpl

Edit `BankingKnowledgeServiceImpl.java` and add documents in `initializeKnowledgeBase()`:

```java
addDocument("""
    Your new banking knowledge document here.
    Can be multi-line.
    """, "category:new,type:document");
```

## 🔧 Configuration Details

### Embedding Model Settings

```properties
# Embedding model (must be pulled in Ollama)
spring.ai.ollama.embedding.model=nomic-embed-text

# Temperature for embeddings (usually 0.0 for consistency)
spring.ai.ollama.embedding.options.temperature=0.0
```

### Vector Store Settings

```properties
# Vector store type: simple or pgvector
spring.ai.vectorstore.type=simple

# For pgvector, additional settings:
spring.ai.vectorstore.pgvector.schema=public
spring.ai.vectorstore.pgvector.table=banking_knowledge_vectors
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE
```

## 🎓 RAG Benefits in Banking Application

### 1. **Enhanced Accuracy**
- AI responses based on actual banking regulations
- Reduces hallucinations
- Ensures compliance with industry standards

### 2. **Up-to-Date Knowledge**
- Easy to update knowledge base
- No need to retrain models
- Quick policy updates

### 3. **Context-Aware Responses**
- Retrieves relevant information for each query
- Combines multiple knowledge sources
- Provides comprehensive answers

### 4. **Audit Trail**
- Can track which documents influenced responses
- Transparency in AI decision-making
- Regulatory compliance

## 🚨 Troubleshooting

### Issue: Embedding Model Not Found

**Error**: `Model 'nomic-embed-text' not found`

**Solution**:
```bash
ollama pull nomic-embed-text
```

### Issue: Vector Store Not Initializing

**Error**: `VectorStore not initialized`

**Solution**:
1. Check if Ollama is running
2. Verify embedding model is available
3. Check application logs for errors

### Issue: No Relevant Results

**Problem**: RAG returns empty results

**Solution**:
1. Verify knowledge base is loaded (check logs)
2. Try broader search queries
3. Increase `topK` parameter
4. Check if documents are properly embedded

### Issue: Slow Performance

**Problem**: RAG queries are slow

**Solution**:
1. Reduce `topK` value
2. Use PgVectorStore for better performance
3. Optimize embedding model
4. Cache frequent queries

## 📈 Performance Optimization

### 1. **Adjust Top-K**
- Lower `topK` = Faster, less context
- Higher `topK` = Slower, more context
- Recommended: 3-5 for most use cases

### 2. **Use Production Vector Store**
- PgVectorStore is faster for large datasets
- Supports indexing
- Better for concurrent access

### 3. **Cache Frequent Queries**
- Implement caching for common queries
- Reduce embedding generation overhead
- Improve response times

## 🔐 Security Considerations

1. **Knowledge Base Access**: Ensure only authorized documents are added
2. **Data Privacy**: Don't include PII in knowledge base
3. **Audit Logging**: Log all RAG queries for compliance
4. **Access Control**: Restrict RAG endpoint access

## 📚 Additional Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Ollama Embeddings](https://ollama.com/library/nomic-embed-text)
- [Vector Databases Explained](https://www.pinecone.io/learn/vector-database/)

## ✅ Verification Checklist

- [ ] Ollama is running
- [ ] Embedding model (`nomic-embed-text`) is pulled
- [ ] Application starts without errors
- [ ] Knowledge base is initialized (check logs)
- [ ] RAG search endpoint works
- [ ] Services return enhanced responses with RAG context

---

**Note**: RAG significantly improves the quality of AI responses by grounding them in actual banking knowledge and regulations. This is especially important for compliance and regulatory use cases.

