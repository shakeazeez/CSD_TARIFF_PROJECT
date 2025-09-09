package com.tariff.calculation.tariffCalc.dto.tariffApiDto;

public record TariffQueryDTO (
    String reportingCountry,
    String partnerCountry,
    String item
) 
{}