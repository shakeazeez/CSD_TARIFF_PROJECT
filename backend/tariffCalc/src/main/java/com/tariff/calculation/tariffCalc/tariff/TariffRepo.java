package com.tariff.calculation.tariffCalc.tariff;

import java.util.Optional;

import com.tariff.calculation.tariffCalc.item.Item;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffRepo extends JpaRepository<Integer, Tariff> {
    // Getters 
    public Optional<Tariff> findByReportingCountryAndItemAndPartnerCountry (String reportingCountry, Item item, String partnerCountry);
    
} 