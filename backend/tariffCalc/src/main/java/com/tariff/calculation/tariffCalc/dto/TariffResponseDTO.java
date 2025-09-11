package com.tariff.calculation.tariffCalc.dto;
import  com.tariff.calculation.tariffCalc.dto.tariffApiDto.TariffRate;

/** 
 * DTO for tariff query response. 
 * 
*/
public record TariffResponseDTO(
    String reportingCountry,
    String partnerCountry,
    String item,
    TariffRate tariffRate,
    Double tariffAmount,
    Double itemCostWithTariff
) {}
