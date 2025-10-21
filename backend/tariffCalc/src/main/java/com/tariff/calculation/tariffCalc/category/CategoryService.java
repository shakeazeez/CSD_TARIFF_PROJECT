package com.tariff.calculation.tariffCalc.category;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tariff.calculation.tariffCalc.service.EmbeddingService;
import com.tariff.calculation.tariffCalc.utility.LemmaUtils;

import jakarta.annotation.PostConstruct;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private EmbeddingService embeddingService;

    @PostConstruct
    public void initializeCategories() {
        try {
            log.info("CategoryService: Starting category initialization...");
            
            // Check if OpenAI API key is available
            String apiKey = LemmaUtils.getEnvOrDotenv("OPEN_AI_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                apiKey = LemmaUtils.getEnvOrDotenv("OPENAI_API_KEY");
            }
            log.info("CategoryService: API key found: {}", apiKey != null && !apiKey.trim().isEmpty());
            
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.info("CategoryService: No API key found, skipping initialization");
                // Skip initialization if no API key (e.g., during testing)
                return;
            }

            long categoryCount = categoryRepo.count();
            log.info("CategoryService: Current category count: {}", categoryCount);
            
            if (categoryCount == 0) {
                log.info("CategoryService: Initializing {} industries...", Industry.values().length);
                Arrays.stream(Industry.values()).forEach(industry -> {
                    log.info("CategoryService: Creating category for: {}", industry.getName());
                    Category category = new Category();
                    category.setName(industry.getName());
                    category.setDesc(industry.getDescription());
                    float[] emb = embeddingService.getEmbedding(industry.getDescription());
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < emb.length; i++) {
                        if (i > 0) sb.append(",");
                        sb.append(emb[i]);
                    }
                    sb.append("]");
                    category.setEmbedding(sb.toString());
                    categoryRepo.save(category);
                    log.info("CategoryService: Saved category: {}", industry.getName());
                });
                log.info("CategoryService: Category initialization completed");
            } else {
                log.info("CategoryService: Categories already exist, skipping initialization");
            }
        } catch (Exception e) {
            log.error("CategoryService: Error during category initialization: {}", e.getMessage(), e);
            // If table doesn't exist yet or API call fails, skip initialization
            // This allows the application to start without embeddings
        }
    }

    public Category findMostSimilarIndustry(String description) {
        log.debug("CategoryService: Finding similar industry for: {}", description);
        
        float[] embedding = embeddingService.getEmbedding(description);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        String embeddingStr = sb.toString();
        
        log.debug("CategoryService: Using embedding string: {}...", embeddingStr.substring(0, 50));
        
        List<Category> categories = categoryRepo.findClosestCategories(embeddingStr);
        List<String> distanceStrings = categoryRepo.findClosestDistances(embeddingStr);
        List<Double> distances = distanceStrings.stream().map(Double::valueOf).collect(Collectors.toList());
        
        log.info("CategoryService: Found {} categories ordered by distance", categories.size());
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            Double dist = distances.get(i);
            log.info("CategoryService: Rank {}: {} (distance: {})", i + 1, cat.getName(), dist);
        }
        
        Category result = categories.isEmpty() ? null : categories.get(0);
        log.debug("CategoryService: Selected category: {}", result != null ? result.getName() : "null");
        return result;
    }
}