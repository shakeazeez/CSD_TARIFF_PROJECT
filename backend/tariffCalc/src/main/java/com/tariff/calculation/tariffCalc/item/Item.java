package com.tariff.calculation.tariffCalc.item;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tariff.calculation.tariffCalc.tariff.Tariff;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "item")
public class Item {
    // TBD how we store the ID 
    @Id
    private Integer itemCode;
    private String itemName;
    
    @OneToMany
    @JsonIgnore
    private List<Tariff> tariffs;
}