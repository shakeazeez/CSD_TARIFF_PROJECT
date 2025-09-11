package com.tariff.calculation.tariffCalc.item;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ItemRepo extends JpaRepository<Item, Integer> {
    // Getters 
    public Optional<Item> findByItemName(String itemName);
}