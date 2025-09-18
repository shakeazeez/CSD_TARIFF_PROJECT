package com.tariff.calculation.tariffCalc.dto.currentTariffApiDto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MoachDTO (
    @JsonProperty("data")
    List<TariffData> tariffData
)
{}