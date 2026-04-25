package com.mm.Spring.AI.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
public class OpenAIController {

    private final OpenAiChatModel openAiChatModel;

    private final ChatClient chatClient;

    @Autowired
    private EmbeddingModel embeddingModel;

    //Injected from AppConfig
    @Autowired
    private VectorStore vectorStore;

    /*
     * ChatClient created using ChatModel directly, without using the builder pattern.
     * This approach requires the user to explicitly specify which ChatModel
     * (e.g. OpenAiChatModel) is to be used, rather than relying on auto-configuration.
     */
//    public OpenAIController(OpenAiChatModel openAiChatModel) {
//        this.chatClient = ChatClient.create(openAiChatModel);
//    }

    /*
     * ChatClient created using the Builder pattern via Spring's auto-configuration.
     * This approach does NOT directly depend on any specific ChatModel (e.g. OpenAiChatModel).
     * The builder abstracts away the underlying model, making it more flexible and decoupled.
     * Note: The second argument `OpenAiChatModel` is included here only to keep the older
     * `/api/chatmodel` example working — it is NOT mandatory for the ChatClient to function.
     */
//    public OpenAIController(ChatClient.Builder builder, OpenAiChatModel openAiChatModel) {
//        this.chatClient = builder.build();
//        this.openAiChatModel = openAiChatModel;
//    }

    /*
     * ChatClient created using the Builder pattern with an in-memory ChatMemory advisor.
     * The MessageChatMemoryAdvisor wraps a ChatMemory instance (e.g. InMemoryChatMemory)
     * and automatically injects the conversation history as context into every prompt.
     * This enables multi-turn conversations where the model is aware of previous messages,
     * without manually managing the message history on the caller side.
     * Note: The `OpenAiChatModel` argument is included only to keep the `/api/chatmodel`
     * example working — it is NOT required for ChatClient or ChatMemory to function.
     */
    public OpenAIController(ChatClient.Builder builder, OpenAiChatModel openAiChatModel, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        this.openAiChatModel = openAiChatModel;
    }


    @GetMapping("/api/chatmodel/{message}")
    public ResponseEntity<String> respond(@PathVariable String message) {

        String response = "chat model: " + openAiChatModel.call(message);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/chatclient/{message}")
    public ResponseEntity<String> interact(@PathVariable String message) {

        String response = "chat client: " +  chatClient
                .prompt(message)
                .call()
                .content();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/chatclient/metadata/{message}")
    public ResponseEntity<String> prompt(@PathVariable String message) {

        String metadata = "chat client metadata: ";
        String response = "chat client response: ";

        metadata += chatClient
                .prompt(message)
                .call()
                .chatResponse()
                .getMetadata()
                .getModel();

        response += chatClient
                .prompt(message)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();

        return  ResponseEntity.ok(metadata + "\n" + response);
    }

    /*
     * Streams the LLM response token by token using Server-Sent Events (SSE).
     *
     * Key differences from the blocking .call() approach:
     *   .call().content()   → waits for the full response, returns String
     *   .stream().content() → returns Flux<String>, emitting each token as it arrives
     *
     * .prompt().user(message) is the explicit fluent form — preferred over .prompt(message)
     * shorthand as it clearly separates prompt construction from user message assignment.
     *
     * produces = TEXT_EVENT_STREAM_VALUE tells Spring MVC to stream the Flux<String>
     * as an SSE response instead of buffering it into a single JSON response.
     */
    @GetMapping(value = "/api/chatclient/stream/{message}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@PathVariable String message) {
        return chatClient
                .prompt()
                .user(message)
                .stream()
                .content();
    }


    /*
     * Demonstrates Spring AI's PromptTemplate — a way to build dynamic prompts
     * using named placeholders (e.g. {type}, {year}, {lang}) filled at runtime via a Map.
     *
     * Flow:
     *   1. Define a template string with {placeholders}
     *   2. Call promptTemplate.create(Map.of(...)) to produce a fully resolved Prompt object
     *   3. Pass the Prompt to chatClient.prompt(prompt) — same fluent API as before
     *
     * This approach keeps prompt structure separate from runtime values,
     * making prompts reusable, readable, and easy to maintain.
     *
     * @param type  movie genre (e.g. action, comedy, thriller)
     * @param year  release year range (e.g. 2020)
     * @param lang  language of the movie (e.g. English, Hindi, French)
     */
    @PostMapping("/api/chatclient/recommend")
    public ResponseEntity<String> recommend(@RequestParam String type, @RequestParam String year, @RequestParam String lang) {

        String template = """
                Recommend one highly rated {type} movie released around {year}.
                The movie should be in {lang} language.
                Include the following details:
                - Movie title and release year
                - Director
                - Main cast (top 3 actors)
                - Runtime
                - A brief plot summary (2-3 sentences)
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("type", type, "year", year, "lang", lang));

        String response = chatClient
                .prompt(prompt)
                .call()
                .content();

        return ResponseEntity.ok(response);
    }

    /*
     * Converts a text input into a dense vector representation (embedding) using the EmbeddingModel.
     *
     * Key points:
     *   embeddingModel.embed(input) → transforms the raw text into a float[] array
     *   This vector embedding captures semantic meaning and can be used for similarity searches,
     *   clustering, or as input to other ML models.
     *
     * @param input  the text string to be embedded (e.g., "Hello world", product description)
     * @return       float array representing the semantic embedding of the input text
     */
    @PostMapping("/api/embedding")
    public ResponseEntity<float[]> embedding(@RequestParam String input) {
        return ResponseEntity.ok(embeddingModel.embed(input));
    }

    /*
     * Computes the cosine similarity between the embeddings of two input strings.
     * Cosine similarity is a common metric for measuring the semantic similarity between two vectors,
     * with a range of [-1, 1] where 1 means identical, 0 means orthogonal (no similarity), and -1 means opposite.
     */
    @PostMapping("/api/similarity")
    public ResponseEntity<Double> similarity(@RequestParam String input1, @RequestParam String input2) {
        float[] embedding1 = embeddingModel.embed(input1);
        float[] embedding2 = embeddingModel.embed(input2);

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            normA += Math.pow(embedding1[i], 2);
            normB += Math.pow(embedding2[i], 2);
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        if (normA == 0 || normB == 0) {
            return ResponseEntity.ok(0.0); // Avoid division by zero
        } else {
            return ResponseEntity.ok(dotProduct / (normA * normB));
        }
    }

    /*
    * API to return products from the vector database/store based on the similarity of the query embedding with the product embeddings.
    */
    @GetMapping("/api/product")
    public ResponseEntity<List<Document>> product(@RequestParam String query) {
        return ResponseEntity.ok(vectorStore.similaritySearch(query));
    }

}
