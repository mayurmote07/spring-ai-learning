package com.mm.Spring.AI.Utility;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Initializes application data by reading text documents, splitting them into chunks,
 * and storing the resulting embeddings in a VectorStore for semantic search capabilities.
 *
 * This component runs automatically after the Spring context is fully initialized (@PostConstruct),
 * ensuring that the vector store is populated with relevant data before the application starts
 * accepting requests.
 */

//commenting to avoid embedding everytime
//@Component
public class DataInitializer {

    @Autowired
    private VectorStore vectorStore;

    /*
     * Reads product details from a text file, splits the content into manageable chunks using a TokenTextSplitter,
     * and adds the documents to the VectorStore.
     *
     * Key configuration:
     *   - Chunk size: 100 tokens (adjustable for different content types)
     *   - Max chunks: 500 (prevents excessive processing)
     *   - Min chunk size: 30 characters (ensures meaningful content)
     *   - Min embed length: 5 (minimum tokens required for embedding)
     *   - Keep separator: false (removes separators between chunks)
     *
     * This setup enables efficient semantic search over the product data.
     */
    @PostConstruct
    public void initializeData() {
        TextReader reader = new TextReader(new ClassPathResource("product_details.txt"));
//        TokenTextSplitter splitter = TokenTextSplitter.builder().build();

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(100) // Adjust chunk size as needed
                .withMaxNumChunks(500)
                .withMinChunkSizeChars(30)
                .withMinChunkLengthToEmbed(5)
                .withKeepSeparator(false)
                .build();

        List<Document> documents = splitter.split(reader.read());

        vectorStore.add(documents);
    }

}
