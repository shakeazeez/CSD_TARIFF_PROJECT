package com.tariff.calculation.tariffCalc.category;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepo extends JpaRepository<Category, Integer> {

    @Query(value = """
        SELECT * FROM categories 
        ORDER BY embedding <-> cast(:embedding as vector)
        LIMIT 1
        """, nativeQuery = true)
	public Optional<Category> getClosestCategory(float[] embedding);
}