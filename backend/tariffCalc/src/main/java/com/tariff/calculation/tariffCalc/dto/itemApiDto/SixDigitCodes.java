package com.tariff.calculation.tariffCalc.dto.itemApiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SixDigitCodes (
    
    @JsonProperty("searchTerm")
    String searchTerm, 
    
    @JsonProperty("category")
    String category, 
    
    @JsonProperty("note")
    String notes,
    
    @JsonProperty("wto_rank'")
    String ranking,
    
    @JsonProperty("6DigitCode")
    String itemCode,
    
    @JsonProperty("desc")
    String description,
    
    @JsonProperty("HSCodeAccuracy")
    String accuracy
) 
{}