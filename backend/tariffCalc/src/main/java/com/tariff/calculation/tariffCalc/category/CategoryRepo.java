package com.tariff.calculation.tariffCalc.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepo extends JpaRepository<Category, Integer> {

    @Query(value = """
        SELECT * FROM category 
        WHERE embedding IS NOT NULL AND LOWER(name) <> 'other'
        ORDER BY embedding <=> cast(:embedding as vector(1536))
        """, nativeQuery = true)
    public List<Category> findClosestCategories(String embedding);

    @Query(value = "SELECT cast(embedding <=> cast(:embedding as vector(1536)) as text) FROM category WHERE embedding IS NOT NULL AND LOWER(name) <> 'other' ORDER BY embedding <=> cast(:embedding as vector(1536))", nativeQuery = true)
    public List<String> findClosestDistances(String embedding);

    Optional<Category> findByName(String name);
}