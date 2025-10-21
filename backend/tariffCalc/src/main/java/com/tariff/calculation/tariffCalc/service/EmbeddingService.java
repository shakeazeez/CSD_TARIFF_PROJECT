package com.tariff.calculation.tariffCalc.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tariff.calculation.tariffCalc.category.Category;
import com.tariff.calculation.tariffCalc.category.CategoryRepo;

@Service
public class EmbeddingService {
    private final CategoryRepo categoryRepo;

    public EmbeddingService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public float[] getEmbedding(String text) {
        // Hardcoded embeddings for testing
        float[] embedding = new float[1536];
        java.util.Arrays.fill(embedding, 0.0f);
        
        String lowerText = text.toLowerCase();
        if (lowerText.contains("cotton") || lowerText.contains("rice") || lowerText.contains("wheat") || lowerText.contains("agriculture")) {
            embedding[0] = 1.0f; // AGRICULTURE
        } else if (lowerText.contains("car") || lowerText.contains("machine") || lowerText.contains("manufacture") || lowerText.contains("steel")) {
            embedding[1] = 1.0f; // MANUFACTURING
        } else {
            embedding[2] = 1.0f; // OTHER
        }
        
        return embedding;
    }

    public Category getEmbeddings(String... args) {
        String combinedText = String.join(" ", args);
        float[] embedding = getEmbedding(combinedText);
        String embeddingStr = "[" + java.util.Arrays.toString(embedding).replaceAll("[\\[\\]\\s]", "") + "]";
        
        List<Category> categories = categoryRepo.findClosestCategories(embeddingStr);
        if (categories.isEmpty()) {
            throw new RuntimeException("No matching industry found for item: " + combinedText);
        }
        Category category = categories.get(0);
        
        return category;
    }
}
