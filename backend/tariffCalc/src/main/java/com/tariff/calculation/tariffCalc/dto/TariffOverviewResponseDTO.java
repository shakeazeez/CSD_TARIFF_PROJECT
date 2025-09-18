package com.tariff.calculation.tariffCalc.dto;

import java.util.List;

/* 
 * DTO to display changes in tariff rates over a specific period.
 */
public record TariffOverviewResponseDTO (
    String reportingCountry,
    String partnerCountry,
    List<HistoricalTariffData> tariffData
) {}
