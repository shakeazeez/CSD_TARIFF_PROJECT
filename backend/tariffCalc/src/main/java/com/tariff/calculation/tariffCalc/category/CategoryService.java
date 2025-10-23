package com.tariff.calculation.tariffCalc.category;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tariff.calculation.tariffCalc.service.EmbeddingService;

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

            long categoryCount = categoryRepo.count();
            log.info("CategoryService: Current category count: {}", categoryCount);
            
            if (categoryCount == 0) {
                log.info("CategoryService: Initializing {} industries...", Industry.values().length);
                Arrays.stream(Industry.values()).forEach(industry -> {
                    log.info("CategoryService: Creating category for: {}", industry.getName());
                    Category category = new Category();
                    category.setName(industry.getName());
                    category.setDesc(industry.getDescription());
                    // Do not embed 'Other' to avoid skewing similarity; leave embedding null
                    if (!"other".equalsIgnoreCase(industry.getName())) {
                        float[] emb = embeddingService.getEmbedding(industry.getDescription());
                        category.setEmbedding(emb);
                    }
                    categoryRepo.save(category);
                    log.info("CategoryService: Saved category: {}", industry.getName());
                });
                log.info("CategoryService: Category initialization completed");
            } else {
                log.info("CategoryService: Categories already exist, checking for missing embeddings...");
                List<Category> all = categoryRepo.findAll();
                long missing = all.stream().filter(c -> c.getEmbedding() == null || c.getEmbedding().length == 0).count();
                if (missing > 0) {
                    log.info("CategoryService: Backfilling embeddings for {} categories...", missing);
                    for (Category c : all) {
                        // Skip 'Other' entirely for embedding/backfill
                        if ("other".equalsIgnoreCase(c.getName())) {
                            // If 'Other' currently has an embedding, clear it to avoid influencing KNN
                            if (c.getEmbedding() != null && c.getEmbedding().length > 0) {
                                c.setEmbedding(null);
                                categoryRepo.save(c);
                                log.debug("CategoryService: Cleared embedding for 'Other'");
                            }
                            continue;
                        }
                        if (c.getEmbedding() == null || c.getEmbedding().length == 0) {
                            float[] emb = embeddingService.getEmbedding(c.getDesc() != null ? c.getDesc() : c.getName());
                            c.setEmbedding(emb);
                            categoryRepo.save(c);
                            log.debug("CategoryService: Backfilled embedding for: {}", c.getName());
                        }
                    }
                    log.info("CategoryService: Backfill complete");
                } else {
                    log.info("CategoryService: All categories already have embeddings");
                }
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