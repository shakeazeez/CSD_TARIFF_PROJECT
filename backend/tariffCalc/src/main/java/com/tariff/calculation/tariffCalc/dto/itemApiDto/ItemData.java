package com.tariff.calculation.tariffCalc.dto.itemApiDto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemData (
    @JsonProperty("six_digit_codes")
    List<SixDigitCodes> codes
) {}