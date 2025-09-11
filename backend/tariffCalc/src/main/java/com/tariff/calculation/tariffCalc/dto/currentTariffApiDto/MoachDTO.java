package com.tariff.calculation.tariffCalc.dto.currentTariffApiDto;

import java.util.List;

public record MoachDTO (
    List<TariffData> tariffData
)
{}