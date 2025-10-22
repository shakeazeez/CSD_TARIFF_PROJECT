package com.tariff.calculation.tariffCalc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tariff.calculation.tariffCalc.category.Category;
import com.tariff.calculation.tariffCalc.category.CategoryRepo;

@Service
public class EmbeddingService {
    private final CategoryRepo categoryRepo;

    // Max acceptable DB distance (<->). If top result distance > this, return OTHER.
    @Value("${category.knn.maxDistance:1.0}")
    private double knnMaxDistance;

    // Min acceptable cosine similarity for Java fallback. If < this, return OTHER.
    @Value("${category.cosine.minSimilarity:0.5}")
    private double minCosineSimilarity;

    public EmbeddingService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public float[] getEmbedding(String text) {
        // Hardcoded embeddings for testing
        float[] embedding = new float[1536];
        java.util.Arrays.fill(embedding, 0.0f);
        
        return embedding;
    }

    public Category getEmbeddings(String... args) {
        String combinedText = String.join(" ", args);
        float[] embedding = getEmbedding(combinedText);
        String embeddingStr = "[" + java.util.Arrays.toString(embedding).replaceAll("[\\[\\]\\s]", "") + "]";
        
        // Try DB-side KNN first
        try {
            List<Category> categories = categoryRepo.findClosestCategories(embeddingStr);
            if (categories != null && !categories.isEmpty()) {
                // Also fetch top distance to gate with threshold
                List<String> distsStr = categoryRepo.findClosestDistances(embeddingStr);
                if (distsStr != null && !distsStr.isEmpty()) {
                    try {
                        double topDist = Double.parseDouble(distsStr.get(0));
                        if (topDist <= knnMaxDistance) {
                            return categories.get(0);
                        } else {
                            // Too far: return OTHER (as a non-persisted Category with name 'Other')
                            Category other = new Category();
                            other.setName("Other");
                            other.setDesc("Miscellaneous items not fitting other categories");
                            return other;
                        }
                    } catch (NumberFormatException ignore) {
                        // If parsing fails, just return the top match to avoid blocking
                        return categories.get(0);
                    }
                } else {
                    return categories.get(0);
                }
            }
        } catch (Exception e) {
            // Log and fall back to local similarity computation
            System.err.println("EmbeddingService: DB KNN query failed: " + e.getMessage());
        }

        // Fallback: compute similarity in Java over all categories
        List<Category> allCategories = categoryRepo.findAll();
        if (allCategories == null || allCategories.isEmpty()) {
            throw new RuntimeException("No matching industry found for item: " + combinedText);
        }

        Category best = null;
        double bestSim = Double.NEGATIVE_INFINITY;
        for (Category c : allCategories) {
            float[] vec = c.getEmbedding();
            if (vec == null) continue;
            double sim = cosineSimilarity(embedding, vec);
            if (sim > bestSim) {
                bestSim = sim;
                best = c;
            }
        }

        if (best == null) {
            throw new RuntimeException("No matching industry found for item: " + combinedText);
        }
        // Apply cosine similarity threshold for fallback
        if (bestSim < minCosineSimilarity) {
            Category other = new Category();
            other.setName("Other");
            other.setDesc("Miscellaneous items not fitting other categories");
            return other;
        }
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
