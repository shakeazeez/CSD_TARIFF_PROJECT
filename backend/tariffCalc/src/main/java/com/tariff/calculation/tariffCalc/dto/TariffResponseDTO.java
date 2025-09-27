package com.tariff.calculation.tariffCalc.dto;

/** 
 * DTO for tariff query response. 
 * 
*/
public record TariffResponseDTO(
    String reportingCountry,
    String partnerCountry,
    String item,
    Double tariffRate,
    Double tariffAmount,
    Double itemCostWithTariff,
    Integer tariffId
) {}
