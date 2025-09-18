package com.tariff.calculation.tariffCalc.dto.itemApiDto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemRetrievalDTO (
    
    @JsonProperty("data")
    ItemData data
    
    // To be settled whether I want the tariff to be more specific 
) 
{}