package com.tariff.calculation.tariffCalc.tariff;

import org.hibernate.cache.spi.support.AbstractReadWriteAccess.Item;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Tariff {
    // for temporary purposes
    @Id
    private Integer id; 
    private String reportingCountry;
    private String partnerCountry;
    
    @OneToMany
    private Item item;
    private Double percentageRate;
}