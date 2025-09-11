package com.tariff.calculation.tariffCalc.dto.itemApiDto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemRetrievalDTO (
    
    @JsonProperty("six_digit_codes")
    List<SixDigitCodes> codes
    
    // To be settled whether I want the tariff to be more specific 
) 
{}