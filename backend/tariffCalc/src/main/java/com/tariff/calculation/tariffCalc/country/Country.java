package com.tariff.calculation.tariffCalc.country;

import java.util.List;

import com.tariff.calculation.tariffCalc.tariff.Tariff;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "country")
public class Country {
    // temporary cause im not sure how I want to store this yet
    @Id
    private Integer countryNumber;
    private String countryCode;
    private String countryName;
    private Boolean isDeveloping;
    
    @OneToMany(mappedBy = "reportingCountry")
    private List<Tariff> reportingTariff;
    
    @OneToMany(mappedBy = "partnerCountry")
    private List<Tariff> partnerTariff;
}