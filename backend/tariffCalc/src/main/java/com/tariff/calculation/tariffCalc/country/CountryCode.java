package com.tariff.calculation.tariffCalc.country;

import org.springframework.data.annotation.Id;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
public class CountryCode {
    // temporary cause im not sure how I want to store this yet
    @Id
    private Integer countryNumber;
    private String countryCode;
    private String countryName;
    
    
}