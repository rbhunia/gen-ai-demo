# Spring AI API Compatibility Notes

## Version: Spring AI 1.0.0-M3

### Known API Differences

#### 1. OllamaEmbeddingModel
Spring AI auto-configures `EmbeddingModel` from `application.properties`. 
Manual configuration may not be needed if using Spring Boot auto-configuration.

**Auto-configuration properties:**
```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.embedding.model=nomic-embed-text
```

#### 2. VectorStore.similaritySearch()

The API signature may vary. The code includes fallback mechanisms:

**Preferred (newer API):**
```java
SearchRequest searchRequest = SearchRequest.query(query).withTopK(topK);
List<Document> results = vectorStore.similaritySearch(searchRequest);
```

**Fallback (older API):**
```java
List<Document> results = vectorStore.similaritySearch(query);
```

#### 3. EmbeddingResponse.getOutput()

Returns `float[]` not `List<Double>`. The code converts:
```java
float[] embeddingArray = response.getResult().getOutput();
List<Double> embedding = new ArrayList<>();
for (float f : embeddingArray) {
    embedding.add((double) f);
}
```

#### 4. PgVectorStore.Builder

API may vary by version. If auto-configuration doesn't work, check Spring AI documentation for your specific version.

### Troubleshooting

1. **If EmbeddingModel bean is not found:**
   - Ensure `spring-ai-ollama-spring-boot-starter` is in dependencies
   - Check `application.properties` has correct Ollama configuration
   - Spring AI should auto-configure the bean

2. **If VectorStore.similaritySearch() fails:**
   - The code includes try-catch fallbacks
   - Check Spring AI version compatibility
   - Verify VectorStore implementation supports the method

3. **If compilation errors persist:**
   - Run `mvn clean compile` to refresh dependencies
   - Check IDE is using correct Java version (21)
   - Ensure Spring AI BOM is properly imported

### Recommended Approach

1. **Use Spring AI Auto-Configuration:**
   - Let Spring Boot auto-configure EmbeddingModel
   - Configure via `application.properties`
   - Only manually configure if auto-configuration fails

2. **Test with SimpleVectorStore first:**
   - Simplest implementation
   - Good for development
   - No external dependencies

3. **Upgrade to newer Spring AI version if needed:**
   - Check Spring AI releases for latest stable version
   - Some APIs may be more stable in newer versions

