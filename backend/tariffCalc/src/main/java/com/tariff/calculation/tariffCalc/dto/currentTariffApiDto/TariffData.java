package com.tariff.calculation.tariffCalc.dto.currentTariffApiDto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TariffData {
    
    @JsonProperty("hs_code")
    private String hsCode;
    
    @JsonProperty("tariff_rate")
    private TariffRate tariffRate; 
    
    // Dunno if want to keep this temporarily
    @JsonProperty("desc")
    private String description; 
    
    // This one contains all our important resource
    @JsonAlias({"tableData", "countryInformation"})
    private List<TableData> countryInformation;
}