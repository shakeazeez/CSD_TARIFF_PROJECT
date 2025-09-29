package com.tariff.calculation.tariffCalc.dto.currentTariffApiDto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableData {
    
    @JsonAlias({"FTA Conventional Duty", "tariffRegion"})
    private String tariffRegion;
    
    @JsonAlias({"Rate", "tariffRate"}) 
    private String tariffRate;
    
    @JsonAlias({"FTA Code", "countryCode"})
    private String countryCode;
    
    @JsonAlias({"Applicable Country", "country"})
    private String country;
}