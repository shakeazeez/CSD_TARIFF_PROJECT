package com.tariff.calculation.tariffCalc.dto.bankServiceDto;

import com.tariff.calculation.tariffCalc.tariff.TariffDetails;

public record SelectedItemsDTO (
        String[] selectedItems,
        String homeCountry,
        String industry,
        String startDate,
        String endDate
){}
