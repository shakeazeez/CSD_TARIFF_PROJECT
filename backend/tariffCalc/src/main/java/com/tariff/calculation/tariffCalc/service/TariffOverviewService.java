package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.dto.TariffOverviewResponseDTO;

import java.util.List;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.dto.GeneralTariffDTO;
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;

public interface TariffOverviewService {
    public TariffOverviewResponseDTO getTariffOverview(TariffCalculationQueryDTO queryDTO);
    public List<Country> getAllCountries(); 
    public List<GeneralTariffDTO> getAllTariff(Integer tariffId); 
}
