package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto;

import java.time.LocalDate;
import java.util.List;

/* 
 * DTO to display changes in tariff rates over a specific period.
 */
public record PastTariffResponseDTO (
    String reportingCountry,
    String partnerCountry,
    List<PastTariffData> tariffData
) {}
