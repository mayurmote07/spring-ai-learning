package com.mm.Spring.AI.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class OpenAIController {

    private final OpenAiChatModel openAiChatModel;

    private final ChatClient chatClient;

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

        metadata += chatClient.prompt(message)
                .call()
                .chatResponse()
                .getMetadata()
                .getModel();

        response += chatClient.prompt(message)
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
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

}
