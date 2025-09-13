package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;

public interface TariffCalculationService {

    // Getters 
    public TariffResponseDTO getCurrentTariffDetails(TariffCalculationQueryDTO tariffQueryDTO);
    public TariffResponseDTO getPastTariffDetails(TariffCalculationQueryDTO tariffQueryDTO);
}