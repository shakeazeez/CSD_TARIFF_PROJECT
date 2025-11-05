package com.user.dto;

import java.time.LocalDate;
import java.util.Map;

public record BankInfoDTO (
    String industry,
    String originCountry,
    Map<Integer, LocalDate> historyTariffIds
)
{}