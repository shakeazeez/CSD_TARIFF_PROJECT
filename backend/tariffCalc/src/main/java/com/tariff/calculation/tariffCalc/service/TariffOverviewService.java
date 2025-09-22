package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.dto.TariffOverviewResponseDTO;

import java.util.List;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;

public interface TariffOverviewService {
    TariffOverviewResponseDTO getTariffOverview(TariffCalculationQueryDTO queryDTO);
    List<Country> getAllCountries(); 
}
