# Spring AI Features Used in Banking Gen AI Application

This document outlines all the Spring AI features and capabilities utilized in the Banking Gen AI application.

## 🎯 Core Spring AI Features

### 1. **ChatClient API** ✅
**Location**: Used across all service implementations

**Purpose**: High-level, fluent API for interacting with AI models

**Usage Examples**:
```java
// Simple prompt
chatClient.prompt()
    .user("What is my account balance?")
    .call()
    .content();

// With Prompt object
chatClient.prompt(prompt).call().content();
```

**Files Using It**:
- `FraudDetectionServiceImpl.java`
- `TransactionAnalysisServiceImpl.java`
- `CustomerServiceChatbotImpl.java`
- `RiskAssessmentServiceImpl.java`
- `ComplianceServiceImpl.java`
- `SpringAiBoardGameService.java`
- `SelfEvaluatingBoardGameService.java`

**Key Benefits**:
- Fluent, builder-style API
- Type-safe interactions
- Simplified error handling
- Automatic response parsing

---

### 2. **Ollama Integration** ✅
**Location**: `ChatClientConfiguration.java`, `application.properties`

**Purpose**: Integration with local Ollama LLM models

**Configuration**:
```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=llama3.2:3b
spring.ai.ollama.chat.options.temperature=0.7
spring.ai.ollama.chat.options.num-predict=2000
spring.ai.ollama.chat.options.top-p=0.9
```

**Code Usage**:
```java
@Bean
public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
    return ChatClient.builder(ollamaChatModel)
        .defaultSystem("...")
        .build();
}
```

**Features**:
- Local model execution (privacy-preserving)
- Configurable model parameters (temperature, top-p, num-predict)
- Automatic model management
- Support for multiple Ollama models

---

### 3. **PromptTemplate** ✅
**Location**: All service implementations

**Purpose**: Dynamic prompt generation with variable substitution

**Usage Pattern**:
```java
String promptTemplate = """
    Analyze the following transaction:
    Account: {accountNumber}
    Amount: {amount}
    Merchant: {merchantName}
    """;

PromptTemplate template = new PromptTemplate(promptTemplate);
Map<String, Object> variables = new HashMap<>();
variables.put("accountNumber", "ACC001");
variables.put("amount", 5000.00);
variables.put("merchantName", "Merchant XYZ");

Prompt prompt = template.create(variables);
String response = chatClient.prompt(prompt).call().content();
```

**Files Using It**:
- `FraudDetectionServiceImpl.java` - Fraud analysis prompts
- `TransactionAnalysisServiceImpl.java` - Transaction analysis prompts
- `CustomerServiceChatbotImpl.java` - Customer service prompts
- `RiskAssessmentServiceImpl.java` - Risk assessment prompts
- `ComplianceServiceImpl.java` - Compliance check prompts

**Benefits**:
- Reusable prompt templates
- Type-safe variable substitution
- Multi-line prompt support (Java text blocks)
- Context-aware prompt generation

---

### 4. **Prompt Engineering** ✅
**Location**: All service implementations

**Purpose**: Specialized prompts for different banking use cases

**Examples**:

#### Fraud Detection Prompt
```java
String fraudAnalysisPrompt = """
    You are an expert fraud detection analyst for a banking institution.
    Analyze the following transaction and determine if it's potentially fraudulent.
    
    Transaction Details: {transactionDetails}
    Account Information: {accountInfo}
    Recent Transaction History: {recentTransactions}
    
    Please provide:
    1. Risk Score (0.0 to 1.0)
    2. Severity Level (LOW, MEDIUM, HIGH, CRITICAL)
    3. Detailed analysis
    4. Risk factors
    5. Recommendation (APPROVE, REVIEW, BLOCK)
    """;
```

#### Compliance Check Prompt
```java
String compliancePrompt = """
    You are an AML compliance expert.
    Analyze the following information for AML compliance.
    
    Check for:
    1. Suspicious transaction patterns
    2. Unusual account activity
    3. High-risk transactions
    4. Structuring patterns
    5. Unusual geographic patterns
    """;
```

**Features**:
- Role-based prompting ("You are an expert...")
- Structured output requests
- Context-aware instructions
- Multi-step analysis requests

---

### 5. **Default System Messages** ✅
**Location**: `ChatClientConfiguration.java`

**Purpose**: Set default system context for all AI interactions

**Implementation**:
```java
ChatClient.builder(ollamaChatModel)
    .defaultSystem("You are an expert AI assistant specialized in banking, " +
                   "finance, fraud detection, risk assessment, and regulatory compliance. " +
                   "You provide accurate, professional, and helpful responses based on " +
                   "banking industry best practices.")
    .build();
```

**Benefits**:
- Consistent AI behavior across all services
- Domain-specific context
- Reduced prompt verbosity
- Centralized AI personality/role

---

### 6. **AI Response Evaluation** ✅
**Location**: `SelfEvaluatingBoardGameService.java`

**Purpose**: Evaluate AI response quality and relevancy

**Components Used**:
- `RelevancyEvaluator` - Evaluates if response is relevant to the question
- `EvaluationRequest` - Wraps question and answer for evaluation
- `EvaluationResponse` - Contains evaluation results

**Implementation**:
```java
RelevancyEvaluator relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);

EvaluationRequest evaluationRequest = 
    new EvaluationRequest(question.question(), answerText);
EvaluationResponse response = relevancyEvaluator.evaluate(evaluationRequest);

if (!response.isPass()) {
    throw new AnswerNotRelevantException(question.question(), answerText);
}
```

**Features**:
- Automatic quality assessment
- Relevancy checking
- Integration with retry mechanisms
- Self-correcting AI responses

---

### 7. **Structured Output Parsing** ✅
**Location**: All service implementations

**Purpose**: Extract structured data from AI text responses

**Implementation Pattern**:
```java
// AI returns structured text
String aiResponse = """
    RISK_SCORE: 0.85
    SEVERITY: HIGH
    ANALYSIS: Multiple risk factors identified...
    RISK_FACTORS: Large amount, Unusual location, Off-hours
    RECOMMENDATION: BLOCK
    """;

// Parse into structured data
FraudAnalysisResult result = parseFraudAnalysis(aiResponse);
```

**Parsing Logic**:
- Line-by-line parsing
- Keyword-based extraction
- Type conversion (String → Double, etc.)
- Fallback to defaults on parse errors

**Files Using It**:
- `FraudDetectionServiceImpl.parseFraudAnalysis()`
- `RiskAssessmentServiceImpl.parseRiskAssessment()`
- `ComplianceServiceImpl.parseComplianceResult()`
- `TransactionAnalysisServiceImpl.extractKeyFindings()`

---

### 8. **Context-Aware AI Interactions** ✅
**Location**: All service implementations

**Purpose**: Provide rich context to AI for better responses

**Context Building Examples**:

#### Transaction Context
```java
String transactionContext = buildTransactionContext(
    request, account, recentTransactions
);
// Includes: account details, transaction history, patterns
```

#### Customer Context
```java
String bankingContext = buildBankingContext(request);
// Includes: customer info, account details, transaction history
```

#### Risk Context
```java
String riskContext = buildRiskContext(
    account, customer, transactionHistory
);
// Includes: customer profile, account activity, transaction patterns
```

**Benefits**:
- More accurate AI responses
- Domain-specific insights
- Reduced hallucinations
- Better decision-making

---

### 9. **Model Configuration & Tuning** ✅
**Location**: `application.properties`

**Purpose**: Fine-tune AI model behavior

**Configuration Options**:
```properties
# Model Selection
spring.ai.ollama.chat.model=llama3.2:3b

# Temperature (creativity/randomness): 0.0-1.0
spring.ai.ollama.chat.options.temperature=0.7

# Max tokens to generate
spring.ai.ollama.chat.options.num-predict=2000

# Top-p sampling (nucleus sampling)
spring.ai.ollama.chat.options.top-p=0.9
```

**Impact**:
- **Temperature 0.7**: Balanced creativity and accuracy (good for banking)
- **num-predict 2000**: Sufficient for detailed analysis
- **top-p 0.9**: Focused, relevant responses

---

### 10. **Integration with Spring Retry** ✅
**Location**: `GenAiDemoApplication.java`, `ChatClientConfiguration.java`

**Purpose**: Resilient AI service calls with automatic retries

**Configuration**:
```java
@EnableRetry
@SpringBootApplication
public class GenAiDemoApplication { ... }
```

**Properties**:
```properties
spring.retry.max-attempts=3
spring.retry.initial-interval=1000
spring.retry.multiplier=2.0
spring.retry.max-interval=5000
```

**Usage**:
```java
@Retryable(retryFor = AnswerNotRelevantException.class, maxAttempts = 3)
public Answer askQuestion(Question question) {
    // AI call with automatic retry on failure
}
```

**Benefits**:
- Automatic retry on transient failures
- Exponential backoff
- Configurable retry policies
- Improved reliability

---

## 📊 Feature Usage Summary

| Spring AI Feature | Used In | Purpose |
|------------------|---------|---------|
| **ChatClient** | All Services | Primary AI interaction API |
| **Ollama Integration** | Configuration | Local LLM model support |
| **PromptTemplate** | All Services | Dynamic prompt generation |
| **Prompt Engineering** | All Services | Specialized banking prompts |
| **Default System Messages** | Configuration | Consistent AI behavior |
| **Response Evaluation** | SelfEvaluatingBoardGameService | Quality assurance |
| **Structured Output Parsing** | All Services | Extract structured data |
| **Context-Aware Interactions** | All Services | Rich context for AI |
| **Model Configuration** | Properties | Fine-tune AI behavior |
| **Spring Retry Integration** | Application | Resilient AI calls |

---

## 🚀 Advanced Patterns Used

### 1. **Multi-Step AI Analysis**
- Fraud Detection: Transaction → Context → Analysis → Alert
- Risk Assessment: Profile → History → Analysis → Recommendations
- Compliance: Data → Rules → Check → Report

### 2. **Prompt Chaining**
- Build context → Create prompt → Get response → Parse → Store

### 3. **Error Handling**
- Try-catch around AI calls
- Fallback responses
- Logging for debugging

### 4. **Performance Optimization**
- Context caching
- Efficient prompt construction
- Response parsing optimization

---

## 🔮 Spring AI Features NOT Used (Future Enhancements)

1. **Vector Stores / RAG** - For knowledge base retrieval
2. **Function Calling** - For structured tool usage
3. **Streaming Responses** - For real-time updates
4. **Multi-Model Support** - OpenAI, Anthropic, etc.
5. **Embeddings** - For semantic search
6. **Document Processing** - PDF, Word parsing
7. **Image Generation** - For reports/dashboards
8. **Audio Processing** - Voice interactions

---

## 📝 Code Examples

### Complete Service Pattern
```java
@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {
    
    private final ChatClient chatClient;
    
    public FraudDetectionResponse detectFraud(FraudDetectionRequest request) {
        // 1. Build context
        String context = buildContext(request);
        
        // 2. Create prompt template
        PromptTemplate template = new PromptTemplate(promptText);
        Map<String, Object> variables = buildVariables(context);
        
        // 3. Generate prompt
        Prompt prompt = template.create(variables);
        
        // 4. Call AI
        String aiResponse = chatClient.prompt(prompt).call().content();
        
        // 5. Parse response
        FraudAnalysisResult result = parseResponse(aiResponse);
        
        // 6. Return structured data
        return buildResponse(result);
    }
}
```

---

## 🎓 Key Takeaways

1. **ChatClient** is the primary interface for all AI interactions
2. **PromptTemplate** enables dynamic, context-aware prompts
3. **Ollama** provides local, privacy-preserving AI capabilities
4. **Structured parsing** extracts actionable data from AI responses
5. **Context building** significantly improves AI accuracy
6. **Retry mechanisms** ensure reliability in production
7. **Prompt engineering** is crucial for domain-specific applications

---

This application demonstrates a comprehensive use of Spring AI's core features for building production-ready, AI-powered banking applications.

