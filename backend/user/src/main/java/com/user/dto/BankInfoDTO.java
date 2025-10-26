package com.user.dto;

import java.util.List;

public record BankInfoDTO (
    String industry,
    String originCountry,
    List<Integer> historyTariffIds
)
{}