package com.tariff.calculation.tariffCalc.dto.bankServiceDto;

import java.util.*;
import com.tariff.calculation.tariffCalc.tariff.TariffDetails;

public record TariffDetailsforItemDTO (
    int hscode,
    String itemName,
    List<TariffDetails> tariffDetailsList
){}
