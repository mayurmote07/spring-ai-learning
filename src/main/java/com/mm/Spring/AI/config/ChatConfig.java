package com.mm.Spring.AI.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    /*
     * Declares ChatMemory as a Spring-managed singleton bean.
     * This ensures the same memory instance is shared across the entire application context,
     * which is critical for maintaining consistent conversation history across requests.
     *
     * InMemoryChatMemoryRepository  → stores raw messages in memory (lost on restart)
     * MessageWindowChatMemory       → wraps the repo, limits context to last N messages
     *
     * Default behavior when optional config lines are omitted:
     *
     * .chatMemoryRepository(...)  → defaults to InMemoryChatMemoryRepository internally.
     *                               Uncomment to swap with a custom storage backend
     *                               (e.g. database-backed or Redis-backed repository).
     *
     * .maxMessages(n)             → defaults to a window of 20 messages.
     *                               Uncomment to control token usage — fewer messages =
     *                               less context sent to OpenAI = cheaper & faster.
     *                               Tune based on your use case.
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(new InMemoryChatMemoryRepository())
//                .maxMessages(10)
                .build();
    }
}

