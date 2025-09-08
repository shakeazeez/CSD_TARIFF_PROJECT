package com.tariff.calculation.tariffCalc.item;

import java.util.List;

import com.tariff.calculation.tariffCalc.tariff.Tariff;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Item {
    // TBD how we store the ID 
    private Integer itemCode;
    private String itemName;
    private List<Tariff> tariffs;
}