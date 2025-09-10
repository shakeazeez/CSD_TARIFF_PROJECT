package com.tariff.calculation.tariffCalc.tariff;

import java.util.List;

import com.tariff.calculation.tariffCalc.item.Item;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffRepo extends JpaRepository<Tariff, Integer> {
    // Getters 
    public List<Tariff> findByReportingCountryAndItem(String reportingCountry, Item item);
    
} 