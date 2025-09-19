package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.dto.TariffOverviewResponseDTO;

import java.util.List;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewQueryDTO;

public interface TariffOverviewService {
    TariffOverviewResponseDTO getTariffOverview(TariffOverviewQueryDTO queryDTO);
    List<Country> getAllCountries(); 
}
