package com.tariff.calculation.tariffCalc.service;

import java.util.Optional;

import com.tariff.calculation.tariffCalc.dto.TariffQueryDTO;

public interface TariffService {
    // Getters 
    public Integer retrieveTariffPercentage(TariffQueryDTO tariffQueryDTO);
}