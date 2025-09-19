package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Extracts the "observation" property from the "dimensions" section.
 * 
 * The observation contains a "values" array with time period data including start dates
 * that are used to map tariff rates to their corresponding dates.
 * 
 * Ignores other dimension properties like "dataset" and "series".
 */

public record Dimension(
    @JsonProperty("observation")
    List<Observation> observation
) {}
