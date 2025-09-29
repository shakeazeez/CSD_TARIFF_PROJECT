package com.tariff.calculation.tariffCalc.dto;


public record GeneralTariffDTO (
    String reportingCountry,
    String partnerCountry, 
    String item,
    Double tariff,
    String description
) {}