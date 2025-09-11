package com.tariff.calculation.tariffCalc.dto.tariffApiDto;

import java.util.List;

public record MoachDTO (
    List<TariffData> tariffData
)
{}