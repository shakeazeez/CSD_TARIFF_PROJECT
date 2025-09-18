package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Extracts the "start" date string.
 * 
 * Each StartPeriod corresponds to a tariff observation index, allowing us to map
 * tariff rates from the "dataSets" section to their correct time periods.
 * 
 * Ignores other fields like "end", "id", and "name" from the JSON object.
 * 
 * {
 *   "start": "1989-01-01T00:00:00", <--
 *   "end": "1989-12-31T00:00:00",
 *   "id": "1989",
 *   "name": "1989"
 * }
 */
public record StartPeriod(
    @JsonProperty("start")
    String start           // eg: "1989-01-01T00:00:00"
) {}