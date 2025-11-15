# Quick Start Guide

## Prerequisites Setup

### 1. Install Ollama
```bash
# macOS
brew install ollama

# Linux
curl -fsSL https://ollama.com/install.sh | sh

# Windows - Download from https://ollama.com
```

### 2. Start Ollama and Pull Model
```bash
# Start Ollama service
ollama serve

# In another terminal, pull the model
ollama pull llama3.2:3b
```

### 3. Verify Ollama is Running
```bash
curl http://localhost:11434/api/tags
```

## Running the Application

### Option 1: Maven
```bash
mvn clean spring-boot:run
```

### Option 2: Build and Run JAR
```bash
mvn clean package
java -jar target/gen-ai-demo-0.0.1-SNAPSHOT.jar
```

## Testing the Application

### 1. Health Check
```bash
curl http://localhost:8080/api/v1/health
```

### 2. Test Fraud Detection
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
    "transactionDate": "2024-01-15T10:30:00",
    "description": "Large suspicious transaction"
  }'
```

### 3. Test Transaction Analysis
```bash
curl -X POST http://localhost:8080/api/v1/transaction-analysis/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "ACC001",
    "analysisType": "SPENDING_PATTERNS"
  }'
```

### 4. Test Customer Service Chat
```bash
curl -X POST http://localhost:8080/api/v1/customer-service/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What is my account balance?",
    "customerId": "CUST001",
    "accountNumber": "ACC001",
    "context": "ACCOUNT_INFO"
  }'
```

### 5. Test Risk Assessment
```bash
curl -X POST http://localhost:8080/api/v1/risk-assessment/assess \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "ACC001",
    "customerId": "CUST001",
    "includeTransactionHistory": true
  }'
```

### 6. Test Compliance Check
```bash
curl -X POST http://localhost:8080/api/v1/compliance/check \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "ACC001",
    "customerId": "CUST001",
    "complianceType": "AML"
  }'
```

## Access H2 Database Console

1. Navigate to: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:bankingdb`
3. Username: `sa`
4. Password: (leave empty)

## Sample Data

The application automatically seeds sample data on startup:
- 3 customers (CUST001, CUST002, CUST003)
- 4 accounts (ACC001, ACC002, ACC003, ACC004)
- 5 transactions (including suspicious ones for testing)

## Troubleshooting

### Ollama Connection Issues
- Ensure Ollama is running: `ollama serve`
- Check if model is available: `ollama list`
- Verify connection: `curl http://localhost:11434/api/tags`

### Port Already in Use
- Change port in `application.properties`: `server.port=8081`

### Database Issues
- H2 console available at `/h2-console`
- Check application logs for SQL errors

## Next Steps

- Review the full [README.md](README.md) for detailed documentation
- Check [k8s/README.md](k8s/README.md) for Kubernetes deployment
- Explore the API endpoints using the examples above

