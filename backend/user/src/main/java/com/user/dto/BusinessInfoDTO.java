package com.user.dto;

import java.util.List;

public record BusinessInfoDTO (
    String originCountry,
    List<String> itemSold,
    List<String> destinationCountries,
    List<Integer> historyTariffIds
)
{}