# Ollama Setup Guide

## Required Models

This application requires two Ollama models:

1. **Chat Model**: `llama3.2:latest` (or `llama3.2:3b`)
2. **Embedding Model**: `nomic-embed-text`

## Installation

### 1. Install Ollama

If you haven't installed Ollama yet:
- Visit: https://ollama.ai
- Download and install for your operating system

### 2. Pull Required Models

```bash
# Pull the chat model
ollama pull llama3.2:latest

# Pull the embedding model
ollama pull nomic-embed-text
```

### 3. Verify Models

```bash
# List all available models
ollama list

# You should see:
# - llama3.2:latest (or llama3.2:3b)
# - nomic-embed-text:latest
```

### 4. Start Ollama

Ollama should start automatically. If not:

```bash
# Start Ollama server
ollama serve

# Or on macOS/Linux:
ollama serve &
```

### 5. Verify Ollama is Running

```bash
# Check if Ollama is accessible
curl http://localhost:11434/api/tags

# Should return a JSON response with available models
```

## Configuration

The application is configured in `application.properties`:

```properties
# Ollama base URL
spring.ai.ollama.base-url=http://localhost:11434

# Chat model (use the one you have available)
spring.ai.ollama.chat.model=llama3.2:latest

# Embedding model
spring.ai.ollama.embedding.model=nomic-embed-text
```

## Troubleshooting

### Error: "model not found, try pulling it first"

**Solution**: Pull the missing model:
```bash
ollama pull <model-name>
```

### Error: "Connection refused" or "Cannot connect to Ollama"

**Solution**: 
1. Ensure Ollama is running: `ollama serve`
2. Check if port 11434 is accessible: `curl http://localhost:11434/api/tags`
3. Verify the base URL in `application.properties`

### Alternative Models

If you want to use different models:

**Chat Models:**
- `llama3.2:latest` (recommended)
- `llama3.2:3b` (smaller, faster)
- `gemma3:1b` (very small)
- `gpt-oss:20b` (larger, more capable)

**Embedding Models:**
- `nomic-embed-text` (recommended, 274 MB)
- `all-minilm` (smaller alternative)
- `mxbai-embed-large` (larger, more accurate)

Update `application.properties` accordingly.

## Model Sizes

- `llama3.2:latest`: ~2.0 GB
- `nomic-embed-text`: ~274 MB
- Total: ~2.3 GB disk space required

## Performance Tips

1. **Use smaller models for development**: `llama3.2:3b` is faster
2. **Keep models in memory**: Ollama caches recently used models
3. **Monitor resource usage**: Larger models require more RAM

## Next Steps

After setting up Ollama:
1. Start the Spring Boot application
2. The application will automatically initialize the banking knowledge base
3. All AI features should work correctly

