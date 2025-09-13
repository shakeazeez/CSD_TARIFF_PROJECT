package com.tariff.calculation.tariffCalc.item;

import java.util.List;

import com.tariff.calculation.tariffCalc.tariff.Tariff;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Entity
public class Item {
    // TBD how we store the ID 
    @Id
    private Integer itemCode;
    private String itemName;
    
    @OneToMany
    private List<Tariff> tariffs;
}