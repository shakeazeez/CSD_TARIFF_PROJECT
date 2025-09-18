package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.dataSets;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Extracts the "series" property from the "dataSets" section of the WITS API response.
 * 
 * The series contains the actual tariff observation data organized by series keys
 * (e.g., "0:0:0:0:0") which map to observation indices and tariff rates.
 * 
 * Ignores other fields like "action" and "attributes" from the dataSets section.
 * 
 * {
 *   "dataSets": [
 *     {
 *       "action": "Information",
 *       "attributes": [0],
 *       "series": {
 *         "0:0:0:0:0": {
 *           "observations": {...} <--
 *         }
 *       }
 *     }
 *   ]
 * }
 */
public record TariffDataSet(
    @JsonProperty("series")
    TariffSeries series
) {}