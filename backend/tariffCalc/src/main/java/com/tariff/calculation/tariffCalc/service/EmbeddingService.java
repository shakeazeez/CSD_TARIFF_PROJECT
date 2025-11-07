package com.tariff.calculation.tariffCalc.service;

import java.util.List;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tariff.calculation.tariffCalc.category.Category;
import com.tariff.calculation.tariffCalc.category.CategoryRepo;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final CategoryRepo categoryRepo;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // Max acceptable cosine distance (<=>), if top resut distance > this, return OTHER.
    private double knnMaxDistance = 1.0;

    // Min acceptable cosine similarity for Java fallback. If < this, return OTHER.
    private double minCosineSimilarity = 0.5;

    // OpenAI API configuration
    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    // @Value("${spring.ai.openai.embedding.options.model:text-embedding-ada-002}")
    // private String embeddingModel;

    public EmbeddingService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public float[] getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.info("No text provided, create default embedding.");
            return createDefaultEmbedding();
        }

        // Check if OpenAI API key is available
        if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
            log.info("OpenAI API key not configured, using fallback embedding");
            return createFallbackEmbedding(text);
        }

        // Prepare OpenAI API request using Map
        Map<String, Object> requestBody = Map.of(
                "model", "text-embedding-ada-002",
                "input", text.trim());

        // Make API call using WebClient and get JSON string response
        String response = webClient.post()
                .uri("https://api.openai.com/v1/embeddings")
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            // Parse JSON response manually using ObjectMapper
            if (response != null && !response.isEmpty()) {
                JsonNode jsonNode = objectMapper.readTree(response);
                JsonNode embeddingNode = jsonNode.path("data").get(0).path("embedding");

                // convert to float array
                float[] embedding = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                        embedding[i] = (float) embeddingNode.get(i).asDouble();
                }

                log.info("OpenAI embedding generated successfully for: " + text);

                return embedding;
            }

            log.info("Invalid response from OpenAI API, using fallback");
            return createFallbackEmbedding(text);

        } catch (Exception e) {
            log.info("Error calling OpenAI API: " + e.getMessage() + "\n Using fallback");
            return createFallbackEmbedding(text);
        }
    }

    private float[] createDefaultEmbedding() {
        float[] embedding = new float[1536];
        Arrays.fill(embedding, 0.001f); // Small non-zero value
        return embedding;
    }

    private float[] createFallbackEmbedding(String text) {
        // Simple keyword-based fallback when OpenAI is not available
        float[] embedding = new float[1536];
        Arrays.fill(embedding, 0.001f);

        // Add some variation based on text content
        String lowerText = text.toLowerCase();
        int hash = Math.abs(lowerText.hashCode());

        // Distribute hash-based features across dimensions
        for (int i = 0; i < 100; i++) { // Use first 100 dimensions for variation
            embedding[i] = 0.001f + ((hash % (i + 1)) / 1000000.0f);
        }

        log.info("Using fallback embedding for: " + text);
        return embedding;
    }

    public Category getEmbeddings(String... args) {
        String combinedText = String.join(" ", args); // join the item name and description together

        log.info("combinedText: " + combinedText);

        float[] embedding = getEmbedding(combinedText); // get embedding for that item

        // log.info("embedding of item name + description: {}", Arrays.toString(embedding));

        // convert the float array to a string, removes whitespace
        String embeddingStr = "[" + java.util.Arrays.toString(embedding).replaceAll("[\\[\\]\\s]", "") + "]";
        // log.info("embeddingStr: " + embeddingStr);

        // Try DB-side KNN first
        try {
            log.info("ASSIGNMENT PATH: Trying database KNN query for: " + combinedText);

            List<Category> categories = categoryRepo.findClosestCategories(embeddingStr);

            log.info("categories sorted by similarity: " + categories.stream().map(Category::getName).toList());

            if (categories != null && !categories.isEmpty()) {
                // Also fetch top distance to gate with threshold
                System.out.println("inside the first if statement, categories are returned");
                List<String> distsStr = categoryRepo.findClosestDistances(embeddingStr);
                if (distsStr != null && !distsStr.isEmpty()) {
                    try {
                        double topDist = Double.parseDouble(distsStr.get(0));
                        log.info("DB KNN Result: " + categories.get(0).getName() + "(distance: " + topDist + ", threshold: " + knnMaxDistance + ")");
                        if (topDist <= knnMaxDistance) {
                            log.info("ASSIGNED BY: Database KNN - Category: " +categories.get(0).getName());
                            return categories.get(0);
                        } else {
                            log.info("ASSIGNED BY: Database KNN Distance Threshold - Category: Other (distance " + topDist + " > " + knnMaxDistance + ")"); 
                            // Too far: return OTHER (as a non-persisted Category with name 'Other')
                            Category other = new Category();
                            other.setName("Other");
                            other.setDesc("Miscellaneous items not fitting other categories");
                            return other;
                        }
                    } catch (NumberFormatException ignore) {
                        // If parsing fails, just return the top match to avoid blocking
                        log.info("ASSIGNED BY: Database KNN (fallback) - Category: " + categories.get(0).getName());
                        return categories.get(0);
                    }
                } else {
                    log.info("ASSIGNED BY: Database KNN (no distances) - Category: " + categories.get(0).getName());
                    return categories.get(0);
                }
            }
        } catch (Exception e) {
            // Log and fall back to local similarity computation
            log.info("EmbeddingService: DB KNN query failed: " + e.getMessage());
            log.info("ASSIGNMENT PATH: Falling back to Java cosine similarity");
        }

        // Fallback: compute similarity in Java over all categories
        List<Category> allCategories = categoryRepo.findAll();
        log.info("JAVA FALLBACK: Found " + (allCategories != null ? allCategories.size() : 0) + " total categories");
        if (allCategories == null || allCategories.isEmpty()) {
            throw new RuntimeException("No matching industry found for item: " + combinedText);
        }

        Category best = null;
        double bestSim = Double.NEGATIVE_INFINITY;
        for (Category c : allCategories) {
            float[] vec = c.getEmbedding();
            if (vec == null) {
                log.info("Category " + c.getName() + " has NULL embedding");
                continue;
            }
            double sim = cosineSimilarity(embedding, vec);
            log.info("Category " + c.getName() + " similarity: " + sim);
            if (sim > bestSim) {
                bestSim = sim;
                best = c;
            }
        }
      
        if (best == null) {
            throw new RuntimeException("No matching industry found for item: " + combinedText);
        }

        log.info("Java Cosine Result: " + best.getName() + " (similarity: "
        + bestSim + ", threshold: " + minCosineSimilarity + ")");

        // Apply cosine similarity threshold for fallback
        if (bestSim < minCosineSimilarity) {
            // System.out.println("ASSIGNED BY: Java Cosine Similarity Threshold - Category:
            // Other (similarity " + bestSim + " < " + minCosineSimilarity + ")");
            Category other = new Category();
            other.setName("Other");
            other.setDesc("Miscellaneous items not fitting other categories");
            return other;
        }
        log.info("ASSIGNED BY: Java Cosine Similarity - Category: " + best.getName());
        return best;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null) return -1.0;
        int len = Math.min(a.length, b.length);
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < len; i++) {
            double va = a[i];
            double vb = b[i];
            dot += va * vb;
            na += va * va;
            nb += vb * vb;
        }
        if (na == 0.0 || nb == 0.0) return -1.0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
