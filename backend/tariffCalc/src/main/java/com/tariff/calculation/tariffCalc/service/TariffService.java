package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.dto.tariffApiDto.TariffQueryDTO;

public interface TariffService {
    // Getters 
    public Integer retrieveTariffPercentage(TariffQueryDTO tariffQueryDTO);
}