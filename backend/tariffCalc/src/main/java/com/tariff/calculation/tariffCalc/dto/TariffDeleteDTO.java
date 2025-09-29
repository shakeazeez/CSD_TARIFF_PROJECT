package com.tariff.calculation.tariffCalc.dto;

public record TariffDeleteDTO (
    String reportingCountry,
    String partnerCountry,
    String item
) {}