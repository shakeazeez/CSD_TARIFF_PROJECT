package com.tariff.calculation.tariffCalc.country;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CountryCodesRepo extends JpaRepository<Integer, CountryCode> {
    // Getters 
    public Optional<CountryCode> findByCountryName(String countryName);
} 