# Quick Start: RAG and Vector Database

## 🚀 Quick Setup (5 minutes)

### 1. Install Embedding Model
```bash
ollama pull nomic-embed-text
```

### 2. Verify Models
```bash
ollama list
# Should show:
# - llama3.2:3b
# - nomic-embed-text
```

### 3. Start Application
```bash
mvn spring-boot:run
```

### 4. Test RAG
```bash
# Test RAG search
curl "http://localhost:8080/api/v1/rag/search?query=fraud%20detection&topK=3"

# Test Fraud Detection with RAG
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

## ✅ What's Included

- ✅ Vector Store (SimpleVectorStore for dev, PgVectorStore for prod)
- ✅ Embedding Model (Ollama nomic-embed-text)
- ✅ Banking Knowledge Base (10 pre-loaded documents)
- ✅ RAG Service (Semantic search)
- ✅ RAG Integration (Fraud Detection, Customer Service, Compliance)

## 📚 Full Documentation

See [RAG_SETUP_GUIDE.md](RAG_SETUP_GUIDE.md) for complete documentation.

