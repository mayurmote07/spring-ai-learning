# Spring AI — Learning Project

A hands-on project to explore **Spring AI** capabilities, starting with OpenAI integration.  
Built with Spring Boot 4 and Spring AI 2.0.

---

## 🛠 Tech Stack

| Layer        | Technology                        |
|--------------|-----------------------------------|
| Language     | Java 17                           |
| Framework    | Spring Boot 4.0.5                 |
| AI Layer     | Spring AI 2.0.0-M4                |
| AI Provider  | OpenAI (GPT)                      |
| Build Tool   | Maven                             |

---

## 📁 Project Structure

```
src/
└── main/
    ├── java/com/mm/Spring/AI/
    │   ├── SpringAiApplication.java         # Entry point
    │   ├── config/
    │   │   └── ChatConfig.java              # Spring beans (ChatMemory)
    │   ├── controller/
    │   │   └── OpenAIController.java        # REST endpoints
    │   └── Utility/
    │       └── DataInitializer.java         # Initializes vector store with product data
    └── resources/
        └── application.properties           # App config (API key)
```

---

## ⚙️ Configuration

Set your OpenAI API key in `src/main/resources/application.properties`:

```properties
spring.application.name=Spring-AI
spring.ai.openai.api-key=YOUR_OPENAI_API_KEY
```

> ⚠️ **Never commit your real API key to version control.** Use environment variables or a secrets manager in production.

---

## 🚀 Running the Application

```powershell
./mvnw spring-boot:run
```

The server starts at `http://localhost:8080`.

---

## 📡 API Endpoints

### 1. Chat via `ChatModel` (low-level)

```
GET /api/chatmodel/{message}
```

Directly invokes `OpenAiChatModel.call()` — minimal abstraction, raw model call.

**Example:**
```
GET http://localhost:8080/api/chatmodel/What is Spring AI?
```

**Response:**
```
with chat model: Spring AI is a framework that ...
```

---

### 2. Chat via `ChatClient` (high-level fluent API)

```
GET /api/chatclient/{message}
```

Uses Spring AI's fluent `ChatClient` builder pattern — preferred for application-level workflows.

**Example:**
```
GET http://localhost:8080/api/chatclient/What is Spring AI?
```

**Response:**
```
chat client: Spring AI is a framework that ...
```

---

### 3. Chat via `ChatClient` with Response Metadata

```
GET /api/chatclient/metadata/{message}
```

Accesses the full `ChatResponse` object to extract both the **model name** (metadata) and the **response text** — useful for understanding what model served the request.

**Example:**
```
GET http://localhost:8080/api/chatclient/metadata/What is Spring AI?
```

**Response:**
```
chat client metadata: gpt-4o-mini
chat client response: Spring AI is a framework that ...
```

> 💡 Note: this endpoint calls the model **twice** (once for metadata, once for response). A single `.chatResponse()` call can return both — a refactor opportunity as the project grows.

---

### 4. Streaming Response via `ChatClient`

```
GET /api/chatclient/stream/{message}
```

Streams the response **token by token** using `Flux<String>` and Server-Sent Events (SSE).  
Returns `text/event-stream` — words appear in real time as the model generates them.

**Example:**
```
GET http://localhost:8080/api/chatclient/stream/Tell me a story
```

Test streaming in terminal:
```powershell
curl.exe -N http://localhost:8080/api/chatclient/stream/Tell+me+a+story
```

> 💡 Key difference from `.call()`: `.stream().content()` returns a `Flux<String>` instead of a single `String`, enabling real-time token delivery without waiting for the full response.

---

### 5. Movie Recommendation via `PromptTemplate`

```
POST /api/recommend?type={type}&year={year}&lang={lang}
```

Uses Spring AI's `PromptTemplate` to build a structured prompt with named placeholders (`{type}`, `{year}`, `{lang}`).  
The template is filled at runtime with user-supplied values before being sent to the model.

**Example:**
```
POST http://localhost:8080/api/recommend?type=thriller&year=2019&lang=English
```

**Response:**
```
Movie: Parasite (2019)
Director: Bong Joon-ho
Top Cast: Song Kang-ho, Lee Sun-kyun, Cho Yeo-jeong
Runtime: 132 minutes
Summary: A poor family schemes to become employed by a wealthy family ...
```

> 💡 `PromptTemplate` separates prompt structure from input values — reusable, testable, and cleaner than string concatenation.

---

### 6. Text Embedding via `EmbeddingModel`

```
POST /api/embedding?input={text}
```

Converts input text into a dense vector representation (embedding) using the `EmbeddingModel`.  
The embedding captures semantic meaning and can be used for similarity searches, clustering, or other ML tasks.

**Example:**
```
POST http://localhost:8080/api/embedding?input=Hello%20world
```

**Response:**
```
[0.123, -0.456, 0.789, ...]  // float array representing the embedding
```

> 💡 Embeddings are numerical vectors that represent the semantic meaning of text — similar texts have similar vectors.

---

### 7. Cosine Similarity between Texts

```
POST /api/similarity?input1={text1}&input2={text2}
```

Computes the cosine similarity between the embeddings of two input strings.  
Cosine similarity measures semantic similarity, ranging from -1 (opposite) to 1 (identical).

**Example:**
```
POST http://localhost:8080/api/similarity?input1=cat&input2=dog
```

**Response:**
```
0.85  // High similarity between related concepts
```

> 💡 Cosine similarity is calculated as: dot product of vectors divided by the product of their magnitudes.

---

### 8. Product Search via Vector Store

```
GET /api/product?query={search_text}
```

Performs semantic search on product data stored in the `VectorStore`.  
Returns documents most similar to the query based on embedding similarity.

**Example:**
```
GET http://localhost:8080/api/product?query=wireless headphones
```

**Response:**
```
[
  {
    "content": "Product details...",
    "metadata": {...}
  }
]
```

> 💡 Vector stores enable semantic search — finding content by meaning, not just keywords.

---

## 🧠 Key Concepts Explored

| Concept       | Description                                                                 |
|---------------|-----------------------------------------------------------------------------|
| `ChatModel`   | Low-level interface for direct model calls. More control, less convenience. |
| `ChatClient`  | High-level fluent API. Supports prompt templates, options, and future tools like memory and advisors. |
| `ChatResponse` | Full response object from `ChatClient` — contains result text, metadata (model name, usage tokens, etc.). |
| `getMetadata().getModel()` | Extracts the model name (e.g. `gpt-4o-mini`) from the response metadata. |
| `getResult().getOutput().getText()` | Extracts the assistant's text reply from the structured response object. |
| `ChatMemory` + `MessageChatMemoryAdvisor` | Enables multi-turn conversations by automatically injecting message history into every prompt. |
| `MessageWindowChatMemory` | Wraps a `ChatMemoryRepository` and limits context to a sliding window of N messages. |
| `InMemoryChatMemoryRepository` | Default in-memory storage for conversation history — lost on app restart. |
| `.stream().content()` | Returns a `Flux<String>` for real-time token-by-token streaming via SSE instead of waiting for the full response. |
| `PromptTemplate` | Defines a reusable prompt with named `{placeholders}` filled at runtime via a `Map` — separates prompt structure from input data. |
| `EmbeddingModel` | Interface for converting text into dense vector representations (embeddings) that capture semantic meaning. |
| `VectorStore` | Storage system for embeddings and associated documents, enabling efficient similarity searches. |
| `TextReader` | Utility for reading text content from various sources (files, URLs) into Document objects. |
| `TokenTextSplitter` | Splits large text documents into smaller chunks based on token counts, optimizing for embedding models. |
| `Cosine Similarity` | Metric for measuring semantic similarity between two vectors, ranging from -1 to 1. |
| `Semantic Search` | Search method that finds content based on meaning rather than exact keyword matches. |
| `@PostConstruct` | Spring annotation that marks a method to be executed after dependency injection, used for initialization logic. |

---

## 🗄️ Vector Stores Explored

This project demonstrates three different vector store implementations for storing and searching embeddings:

### 1. SimpleVectorStore (In-Memory)
- **Pros**: Easy setup, no external dependencies, fast for small datasets
- **Cons**: Data lost on restart, not suitable for production
- **Use case**: Development, testing, small-scale applications

### 2. PgVectorStore (PostgreSQL with pgvector extension)
- **Pros**: Persistent storage, ACID compliance, scalable
- **Cons**: Requires PostgreSQL setup with pgvector extension
- **Use case**: Production applications needing persistence and scalability

### 3. RedisVectorStore (Redis with vector support)
- **Pros**: High performance, in-memory with optional persistence, distributed
- **Cons**: Requires Redis server, more complex setup
- **Use case**: High-throughput applications, caching layers, real-time search

> 💡 The current implementation uses **RedisVectorStore** for its balance of performance and persistence.

---
