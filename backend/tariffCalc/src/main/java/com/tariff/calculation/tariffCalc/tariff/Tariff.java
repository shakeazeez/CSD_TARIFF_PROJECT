package com.tariff.calculation.tariffCalc.tariff;

import java.time.LocalDate;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.item.Item;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {
    // for temporary purposes because idk how i planning on storing this
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "reporting_country_id")
    private Country reportingCountry;
    
    @ManyToOne
    @JoinColumn(name = "partner_country_id")
    private Country partnerCountry;

    @ManyToOne
    private Item item;
    private Double percentageRate;
    
    private LocalDate localDate;

    public Tariff(Country reportingCountry, Country partnerCountry, Item item, Double percentageRate, LocalDate localDate) {
        this.reportingCountry = reportingCountry;
        this.partnerCountry = partnerCountry;
        this.item = item;
        this.percentageRate = percentageRate;
        this.localDate = localDate;
    }
}
