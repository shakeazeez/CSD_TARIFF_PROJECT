package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto;

import java.time.LocalDate;

public record PastTariffData (
    LocalDate periodStart,
    LocalDate periodEnd,
    double tariffRate
) {}
