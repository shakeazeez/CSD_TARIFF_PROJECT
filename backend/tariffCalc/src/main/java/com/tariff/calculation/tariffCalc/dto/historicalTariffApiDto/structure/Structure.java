package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Extracts the "dimensions" property from the "structure" section of the WITS API response.
 * 
 * The dimensions contain the "observation" property, which has a "values" array where each
 * value contains a "start" date string (e.g., "1989-01-01T00:00:00") that maps to the
 * corresponding tariff observation data from the "dataSets" section.
 * 
 * This mapping allows us to associate each tariff rate with its correct time period.
 * 
 * Currently ignores "name", "description" fields from the structure section.
 * 
 * {
 *   "structure": {
 *     "name": "WITS - UNCTAD TRAINS Tariff Data",
 *     "description": "...",
 *     "dimensions": { <--
 *       "series": [...],
 *       "observation": [...]
 *     }
 *   }
 * }
 */
public record Structure (
    @JsonProperty("dimensions")
    Dimension dimensions
) {}

