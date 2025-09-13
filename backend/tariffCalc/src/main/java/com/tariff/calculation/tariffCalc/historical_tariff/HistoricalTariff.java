package com.tariff.calculation.tariffCalc.historical_tariff;

import java.time.LocalDate;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.item.Item;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalTariff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "reporting_country")
    private Country reportingCountry;

    @OneToMany(mappedBy = "partner_country")
    private Country partnerCountry;

    @OneToMany
    private Item item;
    private Double percentageRate;
    
    private LocalDate localDate;

    public HistoricalTariff(Country reportingCountry, Country partnerCountry, Item item, Double percentageRate, LocalDate localDate) {
        this.reportingCountry = reportingCountry;
        this.partnerCountry = partnerCountry;
        this.item = item;
        this.percentageRate = percentageRate;
        this.localDate = localDate;
    }
}
