package com.tariff.calculation.tariffCalc.country;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CountryRepo extends JpaRepository<Country, Integer> {
    // Getters 
    public Optional<Country> findByCountryName(String countryName);
    public Optional<Country> findByCountryCode(String countryCode);
    public Optional<Country> findFirstByCountryNameContainingIgnoreCase(String name);
} 