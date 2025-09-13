package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.dto.TariffOverviewQueryDTO;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.PastTariffResponseDTO;

public interface TariffOverviewService {
    PastTariffResponseDTO getTariffOverview(TariffOverviewQueryDTO queryDTO);
}
