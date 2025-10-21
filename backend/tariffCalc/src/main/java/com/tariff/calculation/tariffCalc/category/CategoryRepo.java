package com.tariff.calculation.tariffCalc.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepo extends JpaRepository<Category, Integer> {

    @Query(value = """
        SELECT * FROM category 
        WHERE embedding IS NOT NULL
        ORDER BY cast(embedding as vector) <-> cast(:embedding as vector)
        """, nativeQuery = true)
	public List<Category> findClosestCategories(String embedding);

    @Query(value = "SELECT cast(cast(embedding as vector) <-> cast(:embedding as vector) as text) FROM category WHERE embedding IS NOT NULL ORDER BY cast(embedding as vector) <-> cast(:embedding as vector)", nativeQuery = true)
	public List<String> findClosestDistances(String embedding);

    Optional<Category> findByName(String name);
}