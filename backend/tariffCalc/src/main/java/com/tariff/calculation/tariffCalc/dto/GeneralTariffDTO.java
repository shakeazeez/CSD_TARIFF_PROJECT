package com.tariff.calculation.tariffCalc.dto;


public record GeneralTariffDTO (
    String reportingCountry,
    String partnerCountry, 
    Double tariff
) {}