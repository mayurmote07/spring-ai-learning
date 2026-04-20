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
with chat client: Spring AI is a framework that ...
```

---

## 🧠 Key Concepts Explored

| Concept       | Description                                                                 |
|---------------|-----------------------------------------------------------------------------|
| `ChatModel`   | Low-level interface for direct model calls. More control, less convenience. |
| `ChatClient`  | High-level fluent API. Supports prompt templates, options, and future tools like memory and advisors. |

---

## 📌 Roadmap / Coming Soon

- [ ] System prompts & prompt templates
- [ ] Streaming responses
- [ ] Memory / conversation history
- [ ] Tool/function calling
- [ ] Image generation
- [ ] Embeddings & vector store integration

---

## 📝 Notes

- This is a **learning/experimental** project — code will evolve as new Spring AI features are explored.
- Spring AI 2.0.0-M4 is a milestone release; APIs may change in future versions.

