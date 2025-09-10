package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.dto.TariffQueryDTO;

public interface TariffService {
    // Getters 
    public Integer retrieveTariffPercentage(TariffQueryDTO tariffQueryDTO);

}