package com.mm.Spring.AI.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenAIController {

    private final OpenAiChatModel  openAiChatModel;

    private final ChatClient chatClient;

    public OpenAIController(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
        this.chatClient = ChatClient.create(openAiChatModel);
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
}
