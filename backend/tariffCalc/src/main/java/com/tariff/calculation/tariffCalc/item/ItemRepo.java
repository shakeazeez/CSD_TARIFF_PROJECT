package com.tariff.calculation.tariffCalc.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.tariff.calculation.tariffCalc.enums.Industry;
import com.tariff.calculation.tariffCalc.country.Country;

public interface ItemRepo extends JpaRepository<Item, Integer> {
    // Getters 
    public Optional<Item> findByItemName(String itemName);
    public List<Item> findByIndustry(Industry industry);
    public Optional<Item> findByItemNameAndCountry(String itemName, Country country);
}