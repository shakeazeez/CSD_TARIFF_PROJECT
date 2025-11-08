package com.tariff.calculation.tariffCalc.category;

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
    
    @Column(name = "embedding", columnDefinition = "TEXT")
    @Convert(converter = FloatArrayConverter.class)
    private float[] embedding;
}