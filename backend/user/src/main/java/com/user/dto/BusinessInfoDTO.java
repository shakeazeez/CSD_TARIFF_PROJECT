package com.user.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record BusinessInfoDTO (
    String originCountry,
    List<BusinessTariffDTO> tariffs,
    Map<Integer, LocalDate> historyTariffIds
)
{}