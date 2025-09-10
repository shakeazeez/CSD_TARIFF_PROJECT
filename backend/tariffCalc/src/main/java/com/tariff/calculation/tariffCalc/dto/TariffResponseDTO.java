package com.tariff.calculation.tariffCalc.dto;
import  com.tariff.calculation.tariffCalc.dto.tariffApiDto.TariffRate;

public record TariffResponseDTO(
    String reportingCountry,
    String partnerCountry,
    String item,
    TariffRate tariffRate
    // add more fields afterwards (js start with these first)
) {}
