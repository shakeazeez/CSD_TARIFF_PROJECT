package com.tariff.calculation.tariffCalc.dto;

import java.time.LocalDate;

public record HistoricalTariffData(
    LocalDate startPeriod,
    Integer tariffId,
    Double tariffRate,
    Double tariffAmount,
    Double itemCostWithTariff
) {}
