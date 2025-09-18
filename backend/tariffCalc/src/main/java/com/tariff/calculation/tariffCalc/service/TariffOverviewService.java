package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.dto.TariffOverviewResponseDTO;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewQueryDTO;

public interface TariffOverviewService {
    TariffOverviewResponseDTO getTariffOverview(TariffOverviewQueryDTO queryDTO);
}
