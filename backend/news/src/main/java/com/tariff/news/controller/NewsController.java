package com.tariff.news.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tariff.news.history.ChatHistoryService;
import com.tariff.news.history.ChatHistory;
import com.tariff.news.service.NewsEmbeddingService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "News & AI Chat", description = "News article processing and AI chatbot endpoints")
@RequestMapping("/news")
@RestController
@RequiredArgsConstructor
public class NewsController {

    private final NewsEmbeddingService newsEmbeddingService;
    private final ChatHistoryService chatHistoryService;
    private final ObjectMapper objectMapper;

    @Operation(
        summary = "Process news query with AI chatbot",
        description = "Process a user query using AI to find relevant news articles and generate a synthesised response"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Query processed successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = com.tariff.news.dto.NewsResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error during processing")
    })
    @PostMapping("/process")
    public ResponseEntity<com.tariff.news.dto.NewsResponse> processQuery(
            @Parameter(description = "User query string", required = true)
            @RequestParam String query,
            @Parameter(description = "Optional username to save history for")
            @RequestParam(required = false) String username,
            @Parameter(description = "Optional conversation ID to append to existing conversation")
            @RequestParam(required = false) Long conversationId) {
        try {
            // Build context from history if conversationId is provided and user is authenticated
            if (conversationId != null && username != null && !username.isBlank()) {
                String context = buildContextFromHistory(conversationId, username);
                if (context != null && !context.isEmpty()) {
                    query = "CONTEXT_START\n" + context + "\nFollow-up: " + query;
                }
            }

            com.tariff.news.dto.NewsResponse results = newsEmbeddingService.processQuery(query);
                        // Check if chatbot is offline
            if ("CHATBOT_OFFLINE".equals(results.getSynthesizedAnswer())) {
                results.setSynthesizedAnswer("The chat bot is currently offline. Please try again later.");
            }
            // If no articles found, provide a fallback response
            if (results.getArticles() == null || results.getArticles().isEmpty()) {
                results.setSynthesizedAnswer("I couldn't find specific articles on that topic. " + results.getSynthesizedAnswer() + " Please provide more details for better assistance.");
            }
            // If a username is provided, persist the chat history for that user (topic extracted internally)
            if (username != null && !username.isBlank()) {
                String topic = newsEmbeddingService.extractTopic(query);
                // Extract original query from context-wrapped text for saving
                String originalQuery = extractOriginalQuery(query);
                try {
                    com.tariff.news.history.ChatHistory saved = chatHistoryService.save(username, topic, originalQuery, results.getSynthesizedAnswer(), results.getArticles(), conversationId);
                    results.setConversationId(saved.getId());
                } catch (Exception ex) {
                    // Do not fail the whole request if history save fails
                    // Log a simple message to stderr
                    System.err.println("Failed to save chat history: " + ex.getMessage());
                }
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Find similar articles by embedding vector",
        description = "Search for articles semantically similar to the provided embedding vector"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Similar articles found successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = com.tariff.news.article.ArticleEmbedding.class))),
        @ApiResponse(responseCode = "500", description = "Error during similarity search")
    })
    @GetMapping("/search/similar")
    public ResponseEntity<List<com.tariff.news.article.ArticleEmbedding>> findSimilarArticles(
            @Parameter(description = "Embedding string in format '[0.1,0.2,0.3,...]'", required = true)
            @RequestParam String embedding,
            @Parameter(description = "Maximum number of results")
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<com.tariff.news.article.ArticleEmbedding> results = newsEmbeddingService.findSimilarArticles(embedding, limit);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Find articles similar to a natural language query",
        description = "Embeds the query and finds semantically similar articles from the database"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Articles found successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = com.tariff.news.article.ArticleEmbedding.class))),
        @ApiResponse(responseCode = "500", description = "Error during query processing")
    })
    @GetMapping("/search/query")
    public ResponseEntity<List<com.tariff.news.article.ArticleEmbedding>> findArticlesByQuery(
            @Parameter(description = "Natural language query to search for similar articles", required = true)
            @RequestParam String query,
            @Parameter(description = "Maximum number of results")
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<com.tariff.news.article.ArticleEmbedding> results = newsEmbeddingService.findArticlesSimilarToQuery(query, limit);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Find articles by topic",
        description = "Retrieve all stored articles that were found using the specified search topic"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Articles retrieved successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = com.tariff.news.article.ArticleEmbedding.class))),
        @ApiResponse(responseCode = "500", description = "Error retrieving articles")
    })
    @GetMapping("/search/topic/{topic}")
    public ResponseEntity<List<com.tariff.news.article.ArticleEmbedding>> findArticlesByTopic(
            @Parameter(description = "Search topic used to find articles")
            @PathVariable String topic) {
        try {
            List<com.tariff.news.article.ArticleEmbedding> results = newsEmbeddingService.findArticlesByTopic(topic);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/articles")
    @Operation(summary = "Get all articles",
               description = "Retrieves all stored articles")
    public ResponseEntity<List<com.tariff.news.article.ArticleEmbedding>> getAllArticles() {
        try {
            List<com.tariff.news.article.ArticleEmbedding> results = newsEmbeddingService.getAllArticles();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Extract the original user query from a context-wrapped query string.
     * If the query contains context (starts with CONTEXT_START), extract the "Follow-up:" part.
     * Otherwise, return the query as-is.
     */
    private String extractOriginalQuery(String query) {
        if (query != null && query.trim().startsWith("CONTEXT_START")) {
            // Find the "Follow-up:" part
            int followUpIndex = query.indexOf("Follow-up:");
            if (followUpIndex != -1) {
                return query.substring(followUpIndex + "Follow-up:".length()).trim();
            }
        }
        return query;
    }

    private String buildContextFromHistory(Long conversationId, String username) {
        try {
            Optional<ChatHistory> historyOpt = chatHistoryService.findByIdAndUsername(conversationId, username);
            if (historyOpt.isPresent()) {
                ChatHistory history = historyOpt.get();
                String messagesJson = history.getMessages();
                if (messagesJson != null && !messagesJson.trim().isEmpty()) {
                    List<Map<String, Object>> messages = objectMapper.readValue(messagesJson, new TypeReference<List<Map<String, Object>>>(){});
                    if (!messages.isEmpty()) {
                        // Get the last 3 messages for context
                        int startIndex = Math.max(0, messages.size() - 3);
                        List<Map<String, Object>> recentMessages = messages.subList(startIndex, messages.size());
                        
                        StringBuilder context = new StringBuilder();
                        context.append("CONTEXT_START\n");
                        for (Map<String, Object> message : recentMessages) {
                            String queryMsg = (String) message.get("query");
                            String responseMsg = (String) message.get("response");
                            if (queryMsg != null) {
                                // Extract original query if it has context
                                String originalQuery = extractOriginalQuery(queryMsg);
                                context.append("User: ").append(originalQuery).append("\n");
                            }
                            if (responseMsg != null) {
                                context.append("Assistant: ").append(responseMsg).append("\n");
                            }
                        }
                        context.append("CONTEXT_END\n");
                        return context.toString();
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Error building context from history: " + e.getMessage());
        }
        return "";
    }

}