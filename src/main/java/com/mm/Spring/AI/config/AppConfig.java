package com.mm.Spring.AI.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class AppConfig {

    /*
    * return simple vector store bean, shared across the entire application context.
     * This allows the same vector store instance to be used across different components,
     * enabling consistent storage and retrieval of vector embeddings.
     */
//    @Bean
//    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
//        return SimpleVectorStore.builder(embeddingModel).build();
//    }

//    @Bean
//    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
//        return PgVectorStore.builder(jdbcTemplate, embeddingModel).build();
//    }


    @Bean
    public JedisPooled jedisPooled(@Value("${spring.data.redis.host}") String host, @Value("${spring.data.redis.port}") int port) {
        return new JedisPooled(host, port);
    }

     @Bean
     public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel, @Value("${spring.data.redis.index-name:product-index}") String indexName) {
         return RedisVectorStore.builder(jedisPooled, embeddingModel)
                 .indexName(indexName)
                 .initializeSchema(true)
                 .build();
     }

}

