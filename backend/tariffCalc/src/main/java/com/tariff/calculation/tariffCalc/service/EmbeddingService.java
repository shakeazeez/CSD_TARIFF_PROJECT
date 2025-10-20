package com.tariff.calculation.tariffCalc.service;

import java.util.List;

import com.tariff.calculation.tariffCalc.category.Category;
import com.tariff.calculation.tariffCalc.category.CategoryRepo;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;

import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {
    private final EmbeddingModel embeddingModel;
    private final CategoryRepo categoryRepo;

    public EmbeddingService(EmbeddingModel embeddingModel, CategoryRepo categoryRepo) {
        this.embeddingModel = embeddingModel;
        this.categoryRepo = categoryRepo;
    }

    public Category getEmbeddings(String... args) {
        float[] embedding =  this.embeddingModel.call(
            new EmbeddingRequest(List.of(args), null)
        ).getResults().get(0).getOutput();
        
        return categoryRepo.getClosestCategory(embedding).get();
    }
}
