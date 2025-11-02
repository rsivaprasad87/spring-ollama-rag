# Spring Ollama RAG (Demo)

This repository is a runnable demo Spring Boot project that:
- Ingests documents from `docs/` (plain text files),
- Calls Ollama to create embeddings,
- Stores documents + embeddings in H2 (for demo),
- Provides a `/api/chat` endpoint which:
  - Embeds the user query using Ollama,
  - Finds top-k similar document chunks via cosine similarity,
  - If top similarity ≥ threshold, calls Ollama chat with retrieved context and returns the model reply,
  - Otherwise returns a safe "I don't know based on the documents" reply.

## Quick start

1. Install and run [Ollama](https://ollama.ai) locally and pull the models you want (embedding + chat). Example:
   ```bash
   ollama pull mxbai-embed-large
   ollama pull phi4-mini
   ```

2. Build and run the Spring app:
   ```bash
   mvn -f spring-ollama-rag/pom.xml clean package
   java -jar spring-ollama-rag/target/spring-ollama-rag-0.0.1-SNAPSHOT.jar
   ```
3+. Add plain text documents to `docs/` (a sample `sample-doc.txt` is included). The application ingests files at startup.

4. Test the chat endpoint:
   ```bash
   curl -X POST http://localhost:8080/api/chat \
     -H "Content-Type: application/json" \
     -d '{"message":"What does the sample document say about data retention?"}'
   ```

## Notes
-  ollama pull llama3.1 --> This model required more memory. So phi4-mini has beend used .
- ollama rm llama3.1 -> remove the installed models 
- If your Ollama responses have a different JSON shape, adjust `OllamaClient` parsing.
