package com.mm.Spring.AI.controller;

import com.mm.Spring.AI.Model.Movie;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/*
 * Demonstrates Spring AI's output converter capabilities for structuring LLM responses into Java objects.
 *
 * This controller showcases three different converter patterns:
 *   1. ListOutputConverter — converts simple comma-separated lists into List<String>
 *   2. BeanOutputConverter — converts structured JSON into a single Java bean (Movie)
 *   3. BeanOutputConverter with ParameterizedTypeReference — converts JSON arrays into List<Movie>
 *
 * Output converters automatically inject format instructions into prompts, ensuring the LLM returns
 * data in the exact format required for automatic parsing into Java objects.
 * This eliminates manual JSON parsing and type casting, making code cleaner and more maintainable.
 */
@RestController
public class MovieController {

    private final ChatClient chatClient;

    public MovieController(ChatClient.Builder builder) {
         this.chatClient = builder.build();
    }

    /*
     * Retrieves the top 5 movies of a given actor as a list of strings.
     *
     * This endpoint demonstrates the ListOutputConverter — a simple converter that:
     *   1. Injects format instructions into the prompt
     *   2. Sends the prompt to the LLM
     *   3. Parses the response (comma-separated values) into a List<String>
     *
     * The LLM receives instructions like: "Return the output in comma separated values"
     * and returns a response that can be automatically parsed into a list.
     *
     * @param actor  the name of the actor (e.g., "Shahrukh Khan", "Tom Hanks")
     * @return       list of top 5 movie titles as strings
     */
    @GetMapping("/api/movies")
    public List<String> getMovie(@RequestParam String actor) {

        String message = """
                List top 5 movies of {actor}
                {format}
               """;

        ListOutputConverter converter = new ListOutputConverter();

        PromptTemplate template = new PromptTemplate(message);
        Prompt prompt = template.create(Map.of("actor", actor, "format", converter.getFormat()));


        return converter.convert(chatClient.prompt(prompt).call().content());
    }

    /*
     * Retrieves the best/highest-rated movie of a given actor as a structured Movie bean.
     *
     * This endpoint demonstrates BeanOutputConverter<T> for converting LLM output to a single Java object:
     *   1. Creates a BeanOutputConverter targeting the Movie.class type
     *   2. Injects JSON schema instructions into the prompt (e.g., "Return JSON with fields: title, actor, releaseYear...")
     *   3. The LLM returns structured JSON matching the schema
     *   4. The converter automatically parses the JSON into a Movie object
     *
     * This is more powerful than ListOutputConverter — it enables complex nested structures and type safety.
     * The converter validates that the response matches the expected Movie class structure before returning.
     *
     * @param actor  the name of the actor (e.g., "Shahrukh Khan", "Tom Hanks")
     * @return       a Movie object containing title, actor, release year, and other metadata
     */
    @GetMapping("/api/movie")
    public Movie getBestMovie(@RequestParam String actor) {

        String message = """
                give the best movie of {actor}
                {format}
               """;

        BeanOutputConverter<Movie> converter = new BeanOutputConverter<>(Movie.class);

        PromptTemplate template = new PromptTemplate(message);
        Prompt prompt = template.create(Map.of("actor", actor, "format", converter.getFormat()));

        return converter.convert(chatClient.prompt(prompt).call().content());

    }

    /*
     * Retrieves the top 5 movies of a given actor as a list of Movie beans.
     *
     * This endpoint demonstrates advanced BeanOutputConverter usage with ParameterizedTypeReference
     * for converting LLM output to collections of Java objects:
     *   1. Uses ParameterizedTypeReference<List<Movie>> to capture the generic type information
     *   2. Injects a JSON schema for an array of Movie objects into the prompt
     *   3. The LLM returns a JSON array, each element matching the Movie schema
     *   4. The converter automatically parses the entire JSON array into List<Movie>
     *
     * This pattern is essential for handling generic types that would otherwise lose type information
     * at runtime due to Java's type erasure. ParameterizedTypeReference preserves the full type signature
     * needed to deserialize complex, nested generic structures.
     *
     * Key advantages:
     *   - Type-safe: Results are automatically typed as List<Movie>, no casting required
     *   - Validation: Ensures each movie object matches the expected schema
     *   - Error handling: Gracefully handles malformed responses with meaningful error messages
     *
     * @param actor  the name of the actor (e.g., "Shahrukh Khan", "Tom Hanks")
     * @return       list of Movie objects (up to top 5) with full metadata
     */
    @GetMapping("/api/moviesList")
    public List<Movie> getMoviesList(@RequestParam String actor) {

        String message = """
                give the top 5 movies of {actor}
                {format}
               """;

        BeanOutputConverter<List<Movie>> converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<Movie>>() {});

        PromptTemplate template = new PromptTemplate(message);
        Prompt prompt = template.create(Map.of("actor", actor, "format", converter.getFormat()));

        return converter.convert(chatClient.prompt(prompt).call().content());

    }



}
