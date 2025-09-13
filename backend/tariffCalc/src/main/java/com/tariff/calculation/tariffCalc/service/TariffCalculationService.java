package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;

public interface TariffCalculationService {

    // Getters 
    TariffResponseDTO getCurrentTariffDetails(TariffCalculationQueryDTO tariffQueryDTO);

}