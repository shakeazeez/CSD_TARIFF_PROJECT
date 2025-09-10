package com.tariff.calculation.tariffCalc.tariff;

import com.tariff.calculation.tariffCalc.country.CountryCode;
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
public class Tariff {
    // for temporary purposes because idk how i planning on storing this
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "reporting_country")
    private CountryCode reportingCountry;

    @OneToMany(mappedBy = "partner_country")
    private CountryCode partnerCountry;

    @OneToMany
    private Item item;
    private Double percentageRate;

    public Tariff(CountryCode reportingCountry, CountryCode partnerCountry, Item item, Double percentageRate) {
        this.reportingCountry = reportingCountry;
        this.partnerCountry = partnerCountry;
        this.item = item;
        this.percentageRate = percentageRate;
    }
}
