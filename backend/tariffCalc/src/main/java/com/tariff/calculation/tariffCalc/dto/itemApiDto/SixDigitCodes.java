package com.tariff.calculation.tariffCalc.dto.itemApiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SixDigitCodes (
    
    @JsonProperty("searchTerm")
    String searchTerm, 
    
    @JsonProperty("category")
    String category, 
    
    @JsonProperty("note")
    String notes,
    
    @JsonProperty("AccuracyRank")
    String ranking,
    
    @JsonProperty("HSCode")
    String itemCode,
    
    @JsonProperty("desc")
    String description,
    
    @JsonProperty("HSCodeAccuracy")
    String accuracy
) 
{}