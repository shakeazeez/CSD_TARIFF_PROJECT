package com.tariff.calculation.tariffCalc.tariff;

import java.util.List;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.item.Item;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffRepo extends JpaRepository<Tariff, Integer> {
    // Getters 
    public List<Tariff> findByReportingCountryAndItem(Country  reportingCountry, Item item);
    public List<Tariff> findByReportingCountryAndPartnerCountryAndItem(Country reportingCountry, Country partnerCountry, Item item);
} 