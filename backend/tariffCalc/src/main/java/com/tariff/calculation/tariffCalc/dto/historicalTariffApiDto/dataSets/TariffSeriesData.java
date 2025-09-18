package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.dataSets;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.List;

/**
 * Extracts the "observations" map.
 * 
 * The observations map contains time period indices as keys and tariff data arrays as values:
 * - Key (String): time period index ("0", "1", "2", etc.) that corresponds to dates in the "structure" section
 * - Value (List<Object>): array where the first element [0] is the tariff rate
 * 
 * This mapping allows us to link tariff rates with their corresponding time periods
 * using the index from both the observations and the structure's values array.
 * 
 * Ignores "attributes" and "annotations" properties from the series object.
 * 
 * {
 *   "0:0:0:0:0": {
 *     "observations": {
 *       "0": [30.29, ...],  <- Index "0" maps to tariff rate 30.29
 *       "1": [29.5, ...],   <- Index "1" maps to tariff rate 29.5
 *       "2": [10.5, ...]    <- Index "2" maps to tariff rate 10.5
 *     } 
 *   }
 * }
 */
public record TariffSeriesData(
        @JsonProperty("observations")
        Map<String, List<Object>> observations
        // Key = time period index, Value = [tariffRate, ...]
) {}
