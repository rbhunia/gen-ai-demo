# Banking Gen AI Application

A comprehensive Spring AI-powered banking application demonstrating advanced Generative AI capabilities for fraud detection, transaction analysis, risk assessment, compliance checking, and customer service automation.

## 🎯 Overview

This application showcases how Generative AI can be integrated into banking operations to enhance security, improve customer experience, and ensure regulatory compliance. Built with Spring Boot 3.3.5, Java 21, and Spring AI framework, it leverages local Ollama models for AI-powered decision making.

## ✨ Key Features

### 1. **AI-Powered Fraud Detection**
- Real-time transaction analysis using Gen AI
- Risk scoring and severity classification
- Pattern recognition for suspicious activities
- Automated fraud alert generation
- Integration with transaction history for context-aware analysis

### 2. **Intelligent Transaction Analysis**
- Spending pattern analysis
- Category-based breakdown
- Trend identification
- Anomaly detection
- Personalized financial insights and recommendations

### 3. **Customer Service Chatbot**
- Context-aware banking assistant
- Account information queries
- Transaction history inquiries
- Intelligent escalation to human agents
- Banking policy and procedure guidance

### 4. **Risk Assessment Engine**
- Comprehensive customer risk profiling
- Multi-factor risk analysis
- Transaction pattern evaluation
- Risk mitigation recommendations
- Historical risk tracking

### 5. **Regulatory Compliance Checker**
- AML (Anti-Money Laundering) compliance
- KYC (Know Your Customer) verification
- Sanctions screening
- Regulatory reporting
- Compliance audit trails

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 3.3.5
- **Java Version**: 21
- **AI Framework**: Spring AI 1.0.0-M4
- **AI Model**: Ollama (Local LLM - llama3.2:3b)
- **Database**: H2 (Development) / PostgreSQL (Production)
- **ORM**: Spring Data JPA / Hibernate
- **Containerization**: Docker
- **Orchestration**: Kubernetes (Minikube)

### Application Structure
```
src/main/java/com/example/
├── config/              # Configuration classes
├── controller/          # REST API controllers
├── model/              # Domain models and DTOs
├── repository/         # Data access layer
├── service/            # Business logic and AI services
└── exception/          # Custom exceptions
```

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.9+
- Docker (for containerization)
- Minikube (for Kubernetes deployment)
- Ollama installed and running locally

### Local Setup

1. **Install and Start Ollama**
   ```bash
   # Install Ollama (macOS)
   brew install ollama
   
   # Start Ollama service
   ollama serve
   
   # Pull the required model
   ollama pull llama3.2:3b
   ```

2. **Clone and Build**
   ```bash
   git clone <repository-url>
   cd gen-ai-demo
   mvn clean install
   ```

3. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the Application**
   - API Base URL: `http://localhost:8080`
   - H2 Console: `http://localhost:8080/h2-console`
   - Health Check: `http://localhost:8080/api/v1/health`

### Docker Deployment

1. **Build Docker Image**
   ```bash
   docker build -t banking-gen-ai-app:latest .
   ```

2. **Run Container**
   ```bash
   docker run -p 8080:8080 \
     -e SPRING_AI_OLLAMA_BASE_URL=http://host.docker.internal:11434 \
     banking-gen-ai-app:latest
   ```

### Kubernetes Deployment (Minikube)

1. **Start Minikube**
   ```bash
   minikube start
   ```

2. **Create Secrets**
   ```bash
   kubectl apply -f k8s/secrets.yaml
   ```

3. **Deploy PostgreSQL**
   ```bash
   kubectl apply -f k8s/postgres-deployment.yaml
   ```

4. **Deploy Application**
   ```bash
   # Build and load image to minikube
   eval $(minikube docker-env)
   docker build -t banking-gen-ai-app:latest .
   
   # Deploy application
   kubectl apply -f k8s/deployment.yaml
   ```

5. **Access the Application**
   ```bash
   # Get service URL
   minikube service banking-gen-ai-service --url
   ```

## 📡 API Endpoints

### Fraud Detection
```http
POST /api/v1/fraud-detection/analyze
Content-Type: application/json

{
  "accountNumber": "ACC001",
  "transactionType": "DEBIT",
  "amount": 50000.00,
  "currency": "USD",
  "merchantName": "Unknown Merchant",
  "merchantCategory": "OTHER",
  "location": "Unknown Location",
  "transactionDate": "2024-01-15T10:30:00",
  "description": "Large transaction"
}
```

### Transaction Analysis
```http
POST /api/v1/transaction-analysis/analyze
Content-Type: application/json

{
  "accountNumber": "ACC001",
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-01-31T23:59:59",
  "analysisType": "SPENDING_PATTERNS"
}
```

### Customer Service Chat
```http
POST /api/v1/customer-service/chat
Content-Type: application/json

{
  "message": "What is my account balance?",
  "customerId": "CUST001",
  "accountNumber": "ACC001",
  "context": "ACCOUNT_INFO"
}
```

### Risk Assessment
```http
POST /api/v1/risk-assessment/assess
Content-Type: application/json

{
  "accountNumber": "ACC001",
  "customerId": "CUST001",
  "includeTransactionHistory": true,
  "includeComplianceCheck": true
}
```

### Compliance Check
```http
POST /api/v1/compliance/check
Content-Type: application/json

{
  "accountNumber": "ACC001",
  "customerId": "CUST001",
  "complianceType": "AML"
}
```

### Health Check
```http
GET /api/v1/health
```

## 🧪 Testing

### Sample Test Data
The application includes seed data with:
- 3 sample customers
- 4 sample accounts
- 5 sample transactions (including suspicious ones)

### Test Fraud Detection
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
    "description": "Large offshore transaction"
  }'
```

## 🔧 Configuration

### Application Properties
Key configuration options in `application.properties`:

```properties
# Ollama Configuration
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=llama3.2:3b
spring.ai.ollama.chat.options.temperature=0.7
spring.ai.ollama.chat.options.num-predict=2000

# Database Configuration
spring.datasource.url=jdbc:h2:mem:bankingdb
spring.jpa.hibernate.ddl-auto=update
```

### Kubernetes Configuration
For Kubernetes deployment, use the `kubernetes` profile:
```bash
java -jar app.jar --spring.profiles.active=kubernetes
```

## 🎓 Gen AI Features Demonstrated

1. **Prompt Engineering**: Specialized prompts for different banking use cases
2. **Context-Aware Analysis**: AI considers transaction history and customer profile
3. **Structured Output Parsing**: Extracting structured data from AI responses
4. **Multi-Modal Analysis**: Combining multiple data sources for comprehensive insights
5. **Retry Mechanisms**: Resilient AI service calls with automatic retries
6. **Temperature Control**: Adjustable AI creativity for different use cases

## 📊 Database Schema

### Core Entities
- **Customer**: Customer information and KYC status
- **Account**: Bank accounts with balances and status
- **Transaction**: Financial transactions with metadata
- **FraudAlert**: AI-generated fraud detection alerts
- **RiskAssessment**: Comprehensive risk analysis results
- **ComplianceReport**: Regulatory compliance check results

## 🔒 Security Considerations

- Input validation on all API endpoints
- SQL injection protection via JPA
- Sensitive data masking in responses
- Audit trails for all AI-generated decisions
- Secure secret management in Kubernetes

## 📈 Performance Optimization

- Connection pooling for database
- Retry mechanisms for AI service calls
- Efficient query patterns with JPA
- Resource limits in Kubernetes deployments
- Health checks for container orchestration

## 🐛 Troubleshooting

### Ollama Connection Issues
- Ensure Ollama is running: `ollama serve`
- Verify model is available: `ollama list`
- Check base URL in application.properties

### Database Issues
- H2 console available at `/h2-console`
- Check JPA logs for SQL errors
- Verify database connection settings

### Kubernetes Deployment Issues
- Check pod status: `kubectl get pods`
- View logs: `kubectl logs <pod-name>`
- Verify services: `kubectl get services`

## 🚧 Future Enhancements

- [ ] RAG (Retrieval Augmented Generation) with vector store
- [ ] Real-time streaming responses
- [ ] Multi-model support (OpenAI, Anthropic)
- [ ] Advanced analytics dashboard
- [ ] Machine learning model integration
- [ ] GraphQL API support
- [ ] WebSocket for real-time updates

## 📝 License

This project is for demonstration purposes.

## 👥 Author

Built for banking domain Gen AI demonstration.

## 🙏 Acknowledgments

- Spring AI Framework
- Ollama for local LLM support
- Spring Boot team
- Banking industry best practices

---

**Note**: This is a demonstration application. For production use, implement additional security measures, comprehensive testing, and compliance with banking regulations.

