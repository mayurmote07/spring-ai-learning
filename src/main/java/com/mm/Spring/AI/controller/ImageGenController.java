package com.mm.Spring.AI.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageGenController {

    private final ChatClient chatClient;
    private final OpenAiImageModel  openAiImageModel;

    public ImageGenController(ChatClient.Builder builder, OpenAiImageModel openAiImageModel) {
        this.chatClient = builder.build();
        this.openAiImageModel = openAiImageModel;
    }

    /*
    * Generates an image based on a text prompt using OpenAI's image generation capabilities.
     */
    @GetMapping("/api/image/{query}")
    public String genImage(@PathVariable String query) {
        ImagePrompt prompt = new ImagePrompt(query, OpenAiImageOptions.builder()
                .height(1024)
                .width(1024)
                .quality("hd")
                .style("natural")
                .build()
        );

        ImageResponse response = openAiImageModel.call(prompt);
        return response.getResult().getOutput().getUrl();
    }

    /*
    * describe image based on provided prompt and image file
     */
    @PostMapping("/api/describe")
    public String describeImage(@RequestParam String query, @RequestParam MultipartFile image) {

        return chatClient.prompt()
                .user(payload -> payload
                        .text(query)
                        .media(MimeTypeUtils.IMAGE_PNG, image.getResource()))
                .call()
                .content();

    }

}
