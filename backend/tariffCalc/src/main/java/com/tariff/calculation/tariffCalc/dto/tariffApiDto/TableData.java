package com.tariff.calculation.tariffCalc.dto.tariffApiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TableData (
    
    @JsonProperty("FTA Conventional Duty")
    String tariffRegion,
    
    @JsonProperty("Rate") 
    String tariffRate,
    
    @JsonProperty("FTA Code")
    String countryCode,
    
    @JsonProperty("Applicable Country")
    String country
)
{}