package com.tariff.calculation.tariffCalc.category;

import org.hibernate.annotations.ColumnTransformer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
	private String name;
	@Column(name = "description", columnDefinition = "TEXT")
	private String desc;
	
	@Column(columnDefinition = "vector(1536)")
	@ColumnTransformer(read = "embedding::text", write = "cast(? as vector(1536))")
	@Convert(converter = com.tariff.calculation.tariffCalc.category.FloatArrayConverter.class)
	private float[] embedding;
}