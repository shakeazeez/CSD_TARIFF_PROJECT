package com.tariff.calculation.tariffCalc.service;

import java.util.List;

import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;
import com.tariff.calculation.tariffCalc.tariff.Tariff;

public interface TariffCalculationService {

    // Getters 
    public TariffResponseDTO getCurrentTariffDetails(TariffCalculationQueryDTO tariffQueryDTO);
}