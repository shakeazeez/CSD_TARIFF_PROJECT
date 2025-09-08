package com.tariff.calculation.tariffCalc.tariff;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffRepo extends JpaRepository<Integer, Tariff> {
    // Getters 
    public Optional<Tariff> findByReportingCountry(String reportingCountry);
} 