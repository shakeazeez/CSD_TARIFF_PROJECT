package com.tariff.calculation.tariffCalc.dto.bankServiceDto;

    public record TariffItemFilterDTO(
        String homeCountry,
        String industry,
        String startDate,
        String endDate
    ) {}
