package com.tariff.calculation.tariffCalc.dto;

public record TariffQueryDTO (
    String reportingCountry,
    String partnerCountry,
    String item
) 
{}