package com.tariff.calculation.tariffCalc.item;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ItemRepo extends JpaRepository <Integer, String> {
    // Getters 
    public Optional<Item> findByItemName(String itemName);
}