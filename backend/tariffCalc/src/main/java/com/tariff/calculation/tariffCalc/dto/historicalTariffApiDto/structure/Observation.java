package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Extracts the "values" array from the "observation" section.
 * 
 * The values array contains time period objects with start dates that correspond
 * to the tariff observation indices from the "dataSets" section.
 * 
 * Each StartPeriod in the list maps to an observation index (0, 1, 2, etc.)
 * allowing us to match tariff rates with their correct time periods.
 * 
 * {
 *   "observation": [
 *     {
 *       "values": [ <--
 *         {
 *           "start": "1990-01-01T00:00:00",
 *           "end": "1990-12-31T00:00:00",
 *           "id": "1990",
 *           "name": "1990"
 *         },
 *        {... more periods ...}
 *       ]
 *     }
 *   ]
 * }
 */

public record Observation(
    @JsonProperty("values")
    List<StartPeriod> values
) {}
