package com.tariff.calculation.tariffCalc.item;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.tariff.Tariff;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

    @Column(unique = true)
    private String itemName;

    @OneToMany
    @JsonIgnore
    private List<Tariff> tariffs;

    @ManyToOne
    private Country country;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    private Industry industry;
}
