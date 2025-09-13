package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto;

import java.time.LocalDate;

public record WitsDTO (
    LocalDate startDate,
    double tariffRate
) {}
