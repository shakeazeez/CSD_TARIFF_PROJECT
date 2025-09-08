package com.tariff.calculation.tariffCalc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TariffRate (
    @JsonProperty("Unit of Quantity")
    String unitOfQuantity,
    
    @JsonProperty("General Rate of Duty")
    String generalDutyRate,
    
    @JsonProperty("Special Rate of Duty")
    String specialDutyRate,
    
    @JsonProperty("Column 2 Rate of Duty")
    String columnDuty, 
    
    @JsonProperty("Quota Quantity")
    String quotaQuantity,
    
    @JsonProperty("Additional Duties")
    String addDuties,
    
    @JsonProperty("Country")
    String countries
)
{}