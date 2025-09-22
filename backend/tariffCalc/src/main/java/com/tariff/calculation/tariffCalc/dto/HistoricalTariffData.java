package com.tariff.calculation.tariffCalc.dto;

import java.time.LocalDate;

public record HistoricalTariffData(
    LocalDate startPeriod,
    Double tariffRate,
    Double tariffAmount,
    Double itemCostWithTariff
) {}
