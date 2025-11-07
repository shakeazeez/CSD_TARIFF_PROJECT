package com.user.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record BusinessInfoDTO (
    String originCountry,
    List<String> itemsSold,
    List<String> destinationCountries,
    Map<Integer, LocalDate> historyTariffIds
)
{}