package com.tariff.calculation.tariffCalc.dto.tariffApiDto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TariffData (
    
    @JsonProperty("hs_code")
    String hsCode,
    
    @JsonProperty("tariff_rate")
    TariffRate tariffRate, 
    
    // Dunno if want to keep this temporarily
    @JsonProperty("desc")
    String description,
    
    // This one contains all our important resource
    @JsonProperty("tableData")
    List<TableData> countryInformation
) 
{}