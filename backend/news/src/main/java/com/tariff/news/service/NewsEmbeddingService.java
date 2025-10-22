package com.tariff.news.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tariff.news.article.ArticleEmbedding;
import com.tariff.news.article.ArticleEmbeddingRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsEmbeddingService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ArticleEmbeddingRepo articleEmbeddingRepo;

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${thenewsapi.api.key}")
    private String thenewsApiKey;

    @Value("${news.similarity.threshold:0.75}")
    private double similarityThreshold;

    @Value("${news.db.candidate.limit:20}")
    private int dbCandidateLimit;

    @Value("${news.db.return.limit:3}")
    private int dbReturnLimit;

    @Value("${news.api.candidate.limit:8}")
    private int apiCandidateLimit;

    @Value("${news.db.per_article_threshold:0.6}")
    private double perArticleThreshold;

    @Value("${news.queryEmbedding.store:false}")
    private boolean storeQueryEmbedding;

    @Value("${news.db.use_pgvector:false}")
    private boolean usePgvector;

    @Value("${news.api.published_after:2020-01-01}")
    private String apiPublishedAfter;

    /**
     * Create a short GPT-produced context string that ties the article to the user's query.
     */
    private String generateQueryContext(String query, String cleanedText) {
        try {
            String excerpt = cleanedText.length() > 300 ? cleanedText.substring(0, 300) : cleanedText;
            String prompt = "Write a 1-2 sentence summary explaining how this article provides context for the query: '" + query + "'. Article excerpt: '" + excerpt + "'";

            Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 60,
                "temperature", 0.0
            );

            String response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("choices").get(0).path("message").path("content").asText().trim();
        } catch (Exception e) {
            log.warn("Failed to generate query context: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Main method to process the user query and return a list of article embeddings.
     * @param query The user query string.
     * @return List of ArticleEmbedding DTOs.
     */
    public com.tariff.news.dto.NewsResponse processQuery(String query) {
        try {
            // Step 1: Generate embedding for the raw user query (DB-first)
            List<Double> queryEmbedding = generateEmbedding(query);
            log.info("Generated query embedding (len={})", queryEmbedding.size());

            // Step 2: Retrieve stored embeddings and compute cosine similarity (either DB-side KNN or Java-side)
            List<ArticleEmbedding> stored;
            if (usePgvector) {
                // When pgvector is available, use the native repo KNN query to limit candidates server-side.
                try {
                    String qEmbStr = convertEmbeddingToString(queryEmbedding);
                    stored = articleEmbeddingRepo.findClosestArticles(qEmbStr, dbCandidateLimit);
                    log.info("DB-side KNN returned {} candidates (pgvector)", stored.size());
                } catch (Exception ex) {
                    log.warn("DB-side KNN failed, falling back to full scan: {}", ex.getMessage());
                    stored = articleEmbeddingRepo.findAllByEmbeddingIsNotNull();
                }
            } else {
                stored = articleEmbeddingRepo.findAllByEmbeddingIsNotNull();
            }
            log.info("Stored embeddings count: {}", stored.size());

            List<Map.Entry<ArticleEmbedding, Double>> scored = stored.stream()
                .map(e -> Map.entry(e, cosineSimilarity(queryEmbedding, convertStringToEmbedding(e.getEmbedding()))))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(dbCandidateLimit)
                .collect(Collectors.toList());

            if (!scored.isEmpty()) {
                double topSim = scored.get(0).getValue();
                log.info("Top DB similarity = {} (threshold={})", topSim, similarityThreshold);

                if (topSim >= similarityThreshold) {
                    // Hit: return top dbReturnLimit articles
                    List<ArticleEmbedding> picked = scored.stream()
                        .limit(dbReturnLimit)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                    // Convert to DTOs
                    List<com.tariff.news.dto.ArticleEmbedding> dtos = picked.stream()
                            .map(e -> new com.tariff.news.dto.ArticleEmbedding(e.getTitle(), e.getUrl(), e.getCleanedText(), e.getEmbedding(), e.getQueryContext(), e.getLastSeenQuery(), "db"))
                            .collect(Collectors.toList());

                        log.info("DB-first hit: returning {} articles", dtos.size());
                        // Synthesize final answer using the stored queryContext snippets and return
                        String synthesized = synthesizeAnswer(query, dtos);
                        return new com.tariff.news.dto.NewsResponse(synthesized, "db", dtos);
                }
            }

            // Miss: fall back to fetching from TheNewsAPI (existing flow)
            log.info("No DB hit - falling back to TheNewsAPI");
            String topic = extractTopic(query);
            List<Article> candidates = fetchArticles(topic);
            log.info("API returned {} candidates", candidates.size());

            // existing reranking pipeline: embed candidates and return top results
            // Generate embedding for topic for candidate similarity
            List<Double> topicEmbedding = generateEmbedding(topic);

            class CandidateResult {
                Article article;
                String cleanedText;
                String embeddingStr;
                double similarity;

                CandidateResult(Article a, String cleanedText, String embeddingStr, double similarity) {
                    this.article = a;
                    this.cleanedText = cleanedText;
                    this.embeddingStr = embeddingStr;
                    this.similarity = similarity;
                }
            }

            List<CandidateResult> results = new ArrayList<>();
            for (Article candidate : candidates) {
                try {
                    String cleanedText = extractFullText(candidate.getUrl());
                    if (cleanedText.length() < 50) continue;
                    List<Double> articleEmbedding = generateEmbedding(cleanedText);
                    String articleEmbeddingStr = convertEmbeddingToString(articleEmbedding);
                    double sim = cosineSimilarity(topicEmbedding, articleEmbedding);
                    results.add(new CandidateResult(candidate, cleanedText, articleEmbeddingStr, sim));
                } catch (Exception e) {
                    log.error("Error processing candidate {}: {}", candidate.getUrl(), e.getMessage());
                }
            }

            if (results.isEmpty()) {
                throw new RuntimeException("No valid articles could be processed for query: " + query);
            }

            List<CandidateResult> top = results.stream()
                .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                .filter(r -> r.similarity >= perArticleThreshold) // enforce per-article minimum similarity
                .limit(dbReturnLimit)
                .collect(Collectors.toList());

            List<com.tariff.news.dto.ArticleEmbedding> embeddings = new ArrayList<>();
            for (CandidateResult cr : top) {
                // After save/upsert, the entity will have queryContext and lastSeenQuery set; however
                // we already generated queryContext earlier in saveArticleEmbedding, so include it in the DTO.
                saveArticleEmbedding(cr.article.getTitle(), cr.article.getUrl(), cr.cleanedText, cr.embeddingStr, topic);

                // Try to load the saved entity to read back the queryContext/lastSeenQuery for the DTO
                String qCtx = "";
                String lastQ = "";
                try {
                    var found = articleEmbeddingRepo.findByUrl(cr.article.getUrl());
                    if (!found.isEmpty()) {
                        var ent = found.get(0);
                        qCtx = ent.getQueryContext();
                        lastQ = ent.getLastSeenQuery();
                    }
                } catch (Exception e) {
                    log.warn("Could not read saved entity for DTO context: {}", e.getMessage());
                }

                var dto = new com.tariff.news.dto.ArticleEmbedding(
                    cr.article.getTitle(), cr.article.getUrl(), cr.cleanedText, cr.embeddingStr, qCtx, lastQ, "api");
                embeddings.add(dto);
            }

            // Synthesize final answer using the query and the article contexts
            String synthesized = synthesizeAnswer(query, embeddings);
            return new com.tariff.news.dto.NewsResponse(synthesized, "api", embeddings);
        } catch (Exception e) {
            log.error("Error processing query: {}", e.toString(), e);
            throw new RuntimeException("Failed to process query", e);
        }
    }

    /**
     * Synthesize a concise answer using the user query and a small set of article contexts.
     */
    private String synthesizeAnswer(String query, List<com.tariff.news.dto.ArticleEmbedding> articles) {
        try {
            StringBuilder context = new StringBuilder();
            for (int i = 0; i < Math.min(3, articles.size()); i++) {
                var a = articles.get(i);
                if (a.getQueryContext() != null && !a.getQueryContext().isEmpty()) {
                    context.append("- ").append(a.getQueryContext()).append("\n");
                } else if (a.getCleanedText() != null) {
                    String snippet = a.getCleanedText().length() > 300 ? a.getCleanedText().substring(0, 300) : a.getCleanedText();
                    context.append("- ").append(snippet).append("\n");
                }
            }
            String prompt = "You are an expert summarizer. The user asked: '" + query + "'. Based on the following article contexts:\n" + context.toString() + "\nProvide a concise 2-5 sentence answer that directly addresses the user's question. Do NOT include raw URLs in the answer; instead, you may mention the source name (e.g., 'Times of India') if relevant. Keep the answer factual, omit filler, and be concise.";

            Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 250,
                "temperature", 0.2
            );

            String response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("choices").get(0).path("message").path("content").asText().trim();
        } catch (Exception e) {
            log.warn("Failed to synthesize final answer: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Extracts a concise search topic (3-6 words) from the user query using OpenAI GPT API.
     * @param query The user query.
     * @return The extracted topic.
     */
    public String extractTopic(String query) {
        String prompt;
        // For trade/tariff queries, be very specific and focused
        if (query.toLowerCase().contains("trade") || query.toLowerCase().contains("tariff")) {
            prompt = "Extract the core trade/economic topic from this query in 2-4 words. Focus ONLY on trade, tariffs, or economic policy. Ignore social impacts. Query: " + query;
        } else {
            prompt = "Extract a concise search topic (3-6 words) from this user query for news articles. Focus on the main subject. Query: " + query;
        }

        Map<String, Object> requestBody = Map.of(
            "model", "gpt-3.5-turbo",
            "messages", List.of(Map.of("role", "user", "content", prompt)),
            "max_tokens", 15,
            "temperature", 0.1
        );

        String response = webClient.post()
            .uri("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer " + openaiApiKey)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String topic = jsonNode.path("choices").get(0).path("message").path("content").asText().trim();
            log.info("Extracted topic for query '{}': '{}'", query, topic);
            return topic;
        } catch (Exception e) {
            log.error("Error parsing GPT response: {}", e.getMessage());
            throw new RuntimeException("Failed to extract topic", e);
        }
    }

    /**
     * Fetches the top 5 relevant articles from TheNewsAPI based on the topic.
     * @param topic The search topic.
     * @return List of Article objects.
     */
    public List<Article> fetchArticles(String topic) {
        // Single, more targeted search instead of multiple redundant calls
        List<Article> articles = fetchArticlesWithQuery(topic);

        // Remove duplicates and apply light relevance filtering
        log.info("Articles before filtering: {}", articles.stream().map(Article::getTitle).collect(Collectors.toList()));

        List<Article> uniqueArticles = articles.stream()
            .filter(article -> !article.getTitle().isEmpty() && !article.getUrl().isEmpty())
            // Temporarily disable relevance filter to debug
            // .filter(article -> hasMinimalRelevance(article.getTitle(), topic))
            .distinct()
            .limit(3)  // Keep top 3 most relevant
            .collect(Collectors.toList());

        log.info("Articles after filtering: {}", uniqueArticles.stream().map(Article::getTitle).collect(Collectors.toList()));

        log.info("Fetched {} relevant articles for topic: '{}'", uniqueArticles.size(), topic);
        return uniqueArticles;
    }    private boolean hasMinimalRelevance(String title, String topic) {
        String lowerTitle = title.toLowerCase();
        String lowerTopic = topic.toLowerCase();

        // Temporarily disable strict filtering to debug
        // Just ensure at least one keyword matches (including shorter words)
        String[] topicWords = lowerTopic.split(" ");
        boolean hasAnyMatch = false;

        for (String word : topicWords) {
            if (word.length() > 2 && lowerTitle.contains(word)) { // Reduced from 3 to 2
                hasAnyMatch = true;
                break;
            }
        }

        if (!hasAnyMatch) {
            log.info("Filtered out article: '{}' for topic: '{}' (no matching words found)", title, topic);
        } else {
            log.info("Article passed filter: '{}' for topic: '{}'", title, topic);
        }

        return hasAnyMatch;
    }

    private List<Article> fetchArticlesWithQuery(String query) {
        // First try with business category
        log.info("Attempting to fetch articles with business category for query: {}", query);
        List<Article> articles = fetchArticlesWithCategory(query, "business");
        log.info("Business category attempt returned {} articles", articles.size());
        if (articles.isEmpty()) {
            log.info("No articles found with business category, trying without category filter");
            // Fallback: try without category restriction
            articles = fetchArticlesWithCategory(query, null);
            log.info("Fallback attempt (no category) returned {} articles", articles.size());
        }
        return articles;
    }

    private List<Article> fetchArticlesWithCategory(String query, String category) {
        // Use /v1/news/all endpoint with optional category filtering
        // Use UriComponentsBuilder to ensure proper URL-encoding
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString("https://api.thenewsapi.com/v1/news/all")
            .queryParam("api_token", thenewsApiKey)
            .queryParam("search", query)
            .queryParam("language", "en")
            .queryParam("published_after", apiPublishedAfter)
            .queryParam("sort", "relevance_score")
            .queryParam("limit", apiCandidateLimit); // request more candidates for reranking

        if (category != null && !category.isEmpty()) {
            b.queryParam("categories", category);
        }

        String url = b.build().toUriString();
        log.info("Calling TheNewsAPI for query='{}' category='{}'", query, category);

        try {
            String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            List<Article> articles = new ArrayList<>();
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode data = jsonNode.path("data");

            log.info("API response contains {} articles in data array", data.size());

            for (JsonNode articleNode : data) {
                String title = articleNode.path("title").asText();
                String articleUrl = articleNode.path("url").asText();

                log.debug("Processing article: title='{}', url='{}'", title, articleUrl);

                if (!title.isEmpty() && !articleUrl.isEmpty()) {
                    articles.add(new Article(title, articleUrl));
                }
            }

            log.info("Query '{}' with category '{}' returned {} valid articles", query, category, articles.size());
            return articles;

        } catch (Exception e) {
            log.error("Failed to fetch articles for query '{}' with category '{}': {}", query, category, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Extracts the full article text from the URL using Jsoup, removing HTML tags.
     * @param url The article URL.
     * @return The cleaned full text.
     */
    public String extractFullText(String url) throws IOException {
        try {
            log.debug("Extracting text from: {}", url);
            String fullText = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
                .text();
            
            // Clean up the text - remove excessive whitespace
            fullText = fullText.replaceAll("\\s+", " ").trim();
            
            // If text is too short, it might be a bad scrape
            if (fullText.length() < 100) {
                log.warn("Extracted text too short ({} chars) from: {}", fullText.length(), url);
                throw new IOException("Insufficient content extracted");
            }
            
            log.debug("Extracted {} characters from: {}", fullText.length(), url);
            return fullText;
            
        } catch (IOException e) {
            log.error("Failed to extract text from {}: {}", url, e.getMessage());
            throw e;
        }
    }

    /**
     * Generates an embedding vector for the given text using OpenAI embeddings API.
     * @param text The text to embed.
     * @return List of doubles representing the embedding.
     */
    public List<Double> generateEmbedding(String text) {
        Map<String, Object> requestBody = Map.of(
            "model", "text-embedding-ada-002",
            "input", text
        );

        String response = webClient.post()
            .uri("https://api.openai.com/v1/embeddings")
            .header("Authorization", "Bearer " + openaiApiKey)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode embeddingNode = jsonNode.path("data").get(0).path("embedding");
            List<Double> embedding = new ArrayList<>();
            for (JsonNode value : embeddingNode) {
                embedding.add(value.asDouble());
            }
            return embedding;
        } catch (Exception e) {
            log.error("Error parsing embedding response: {}", e.getMessage());
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    /**
     * Converts a List<Double> embedding to string format for storage.
     * @param embedding The embedding vector.
     * @return String representation like "[0.123,-0.456,0.789,...]".
     */
    private String convertEmbeddingToString(List<Double> embedding) {
        return "[" + embedding.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(",")) + "]";
    }

    /**
     * Saves an article embedding to the database.
     * @param title Article title.
     * @param url Article URL.
     * @param cleanedText Cleaned article text.
     * @param embeddingStr String representation of embedding.
     * @param topic The search topic used.
     */
    private void saveArticleEmbedding(String title, String url, String cleanedText, String embeddingStr, String topic) {
        try {
            // Generate small GPT context for RAG
            String queryContext = generateQueryContext(topic, cleanedText);

            // Upsert: if article exists, update fields; else create
            List<ArticleEmbedding> existing = articleEmbeddingRepo.findByUrl(url);
            if (!existing.isEmpty()) {
                ArticleEmbedding entity = existing.get(0);
                entity.setTitle(title);
                entity.setCleanedText(cleanedText);
                entity.setEmbedding(embeddingStr);
                entity.setTopic(topic);
                entity.setQueryContext(queryContext);
                entity.setLastSeenQuery(topic);
                if (storeQueryEmbedding) {
                    List<Double> qe = generateEmbedding(topic + "\n" + cleanedText);
                    entity.setQueryEmbedding(convertEmbeddingToString(qe));
                }
                articleEmbeddingRepo.save(entity);
                log.info("Updated article embedding for: {}", title);
                return;
            }

            ArticleEmbedding entity = new ArticleEmbedding();
            entity.setTitle(title);
            entity.setUrl(url);
            entity.setCleanedText(cleanedText);
            entity.setEmbedding(embeddingStr);
            entity.setTopic(topic);
            entity.setQueryContext(queryContext);
            entity.setLastSeenQuery(topic);
            if (storeQueryEmbedding) {
                List<Double> qe = generateEmbedding(topic + "\n" + cleanedText);
                entity.setQueryEmbedding(convertEmbeddingToString(qe));
            }
            articleEmbeddingRepo.save(entity);
            log.info("Saved article embedding for: {}", title);
        } catch (Exception e) {
            log.error("Error saving article embedding: {}", e.toString(), e);
        }
    }

    /**
     * Searches for similar articles based on an embedding vector.
     * @param embeddingStr The embedding string to search with.
     * @param limit Maximum number of results.
     * @return List of similar articles.
     */
    /**
     * Searches for similar articles based on an embedding vector.
     * @param embeddingStr The embedding string to search with.
     * @param limit Maximum number of results.
     * @return List of similar articles.
     */
    public List<ArticleEmbedding> findSimilarArticles(String embeddingStr, int limit) {
        // Fallback: retrieve all stored embeddings and compute cosine similarity in Java
        List<ArticleEmbedding> all = articleEmbeddingRepo.findAllByEmbeddingIsNotNull();
        List<Double> target = convertStringToEmbedding(embeddingStr);
        return all.stream()
            .map(e -> Map.entry(e, cosineSimilarity(target, convertStringToEmbedding(e.getEmbedding()))))
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Finds articles similar to a user query by embedding the query and searching semantically.
     * @param query The user query.
     * @param limit Maximum number of results.
     * @return List of similar articles.
     */
    public List<ArticleEmbedding> findArticlesSimilarToQuery(String query, int limit) {
        try {
            // Generate embedding for the user query
            List<Double> queryEmbedding = generateEmbedding(query);
            String embeddingStr = convertEmbeddingToString(queryEmbedding);

            // Find similar articles using semantic search (Java-side fallback)
            return findSimilarArticles(embeddingStr, limit);
        } catch (Exception e) {
            log.error("Error finding articles similar to query '{}': {}", query, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Searches for articles by topic.
     * @param topic The search topic.
     * @return List of articles for the topic.
     */
    public List<ArticleEmbedding> findArticlesByTopic(String topic) {
        return articleEmbeddingRepo.findByTopic(topic);
    }

    /**
     * Converts string embedding back to List<Double> for processing.
     * @param embeddingStr The string representation.
     * @return List of doubles.
     */
    public List<Double> convertStringToEmbedding(String embeddingStr) {
        if (embeddingStr == null || embeddingStr.length() < 3) {
            return new ArrayList<>();
        }
        String[] parts = embeddingStr.substring(1, embeddingStr.length() - 1).split(",");
        List<Double> embedding = new ArrayList<>();
        for (String part : parts) {
            try {
                embedding.add(Double.valueOf(part.trim()));
            } catch (NumberFormatException e) {
                log.warn("Invalid embedding value: {}", part);
                embedding.add(0.0);
            }
        }
        return embedding;
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty() || a.size() != b.size()) return 0.0;
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            double va = a.get(i);
            double vb = b.get(i);
            dot += va * vb;
            normA += va * va;
            normB += vb * vb;
        }
        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Inner class for Article
    public static class Article {
        private final String title;
        private final String url;

        public Article(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public String getTitle() { return title; }
        public String getUrl() { return url; }
    }
}