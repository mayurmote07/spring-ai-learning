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
    │   └── controller/
    │       └── OpenAIController.java        # REST endpoints
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

---


## 📝 Notes

- This is a **learning/experimental** project — code will evolve as new Spring AI features are explored.
- Spring AI 2.0.0-M4 is a milestone release; APIs may change in future versions.

