package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.dataSets.TariffDataSet;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure.Structure;

public record WitsDTO (
    @JsonProperty("dataSets") // provides tariff rate in the first column of the observations array
    TariffDataSet dataSets,

    @JsonProperty("structure") // provides time dimension to map observation keys to years
    Structure structure
) {}
