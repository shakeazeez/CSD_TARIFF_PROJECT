package com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.dataSets;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;

/**
 * Extracts the series data from the "series" section of the WITS API response.
 * 
 * In our case, there's typically only one series key ("0:0:0:0:0") that contains
 * all the tariff observations, but we use @JsonAnySetter for flexibility.
 * 
 * {
 *   "series": {
 *     "0:0:0:0:0": {           <- Usually the only key in our responses
 *       "observations": {
 *         "0": [30.29, ...],
 *         "1": [29.5, ...]
 *       }
 *     }
 *   }
 * }
 */
public class TariffSeries {
    private Map<String, TariffSeriesData> seriesData = new HashMap<>();

    /**
     * Dynamically captures any series key-value pair from the JSON response.
     * 
     * Called by Jackson for each property in the "series" object:
     * - key: The series identifier (e.g., "0:0:0:0:0")
     * - value: The TariffSeriesData containing observations array
     * 
     * This allows us to handle dynamic series keys without hardcoding specific keys.
     * 
     * @param key The series key from JSON (typically "0:0:0:0:0")
     * @param value The TariffSeriesData object containing tariff observations
     */
    @JsonAnySetter
    public void setSeriesData(String key, TariffSeriesData value) {
        seriesData.put(key, value);
    }

    /**
     * Provides code-level access to all stored series data.
     * Hidden from JSON serialization to avoid duplicate data.
     */
    @JsonIgnore
    public Map<String, TariffSeriesData> getSeriesData() {
        return seriesData;
    }
}