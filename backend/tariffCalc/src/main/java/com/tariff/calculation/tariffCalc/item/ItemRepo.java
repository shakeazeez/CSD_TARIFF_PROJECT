package com.tariff.calculation.tariffCalc.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.country.Country;

public interface ItemRepo extends JpaRepository<Item, Integer> {
    // Getters 
    public Optional<Item> findByItemName(String itemName);
    public List<Item> findByIndustry(Industry industry);
    @Query("SELECT i FROM Item i WHERE lower(i.itemName) = lower(?1) AND i.country = ?2")
    public Optional<Item> findByItemNameAndCountry(String itemName, Country country);
}