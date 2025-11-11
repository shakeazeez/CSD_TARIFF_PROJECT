package com.tariff.calculation.tariffCalc.dto;

import java.time.LocalDate;

public record GeneralTariffDTO (
    String reportingCountry,
    String partnerCountry, 
    String item,
    Double tariff,
    String description,
    LocalDate date
) {}