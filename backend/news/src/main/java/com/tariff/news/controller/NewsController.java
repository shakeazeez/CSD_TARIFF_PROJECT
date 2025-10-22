package com.tariff.news.controller;

import com.tariff.news.service.NewsEmbeddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "News Controller", description = "News article processing and embedding endpoints")
@RequestMapping("/news")
@RestController
@RequiredArgsConstructor
public class NewsController {

    private final NewsEmbeddingService newsEmbeddingService;

    @PostMapping("/process")
    @Operation(summary = "Process user query and return relevant news articles with embeddings",
               description = "Extracts search topic from query, fetches articles, generates embeddings, and stores them")
    public ResponseEntity<com.tariff.news.dto.NewsResponse> processQuery(
            @Parameter(description = "User query string")
            @RequestParam String query) {
        try {
            com.tariff.news.dto.NewsResponse results = newsEmbeddingService.processQuery(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search/similar")
    @Operation(summary = "Find similar articles based on embedding vector",
               description = "Searches for articles similar to the provided embedding string")
    public ResponseEntity<List<com.tariff.news.article.ArticleEmbedding>> findSimilarArticles(
            @Parameter(description = "Embedding string in format '[0.1,0.2,0.3,...]'")
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

    @GetMapping("/search/query")
    @Operation(summary = "Find articles similar to a natural language query",
               description = "Embeds the query and finds semantically similar articles from the database")
    public ResponseEntity<List<com.tariff.news.article.ArticleEmbedding>> findArticlesByQuery(
            @Parameter(description = "Natural language query to search for similar articles")
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

    @GetMapping("/search/topic/{topic}")
    @Operation(summary = "Find articles by topic",
               description = "Retrieves all articles that were found using the specified search topic")
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
}