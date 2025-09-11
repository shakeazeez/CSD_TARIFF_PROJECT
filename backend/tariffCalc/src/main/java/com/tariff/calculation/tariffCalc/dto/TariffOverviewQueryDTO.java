package com.tariff.calculation.tariffCalc.dto;

import java.time.LocalDate;

/*
 * DTO for displaying tariff overview over a time interval.
 * 
 * NOTE: This file is NOT part of Sprint 1. It is a placeholder for future development.
 * 
 * Purpose:
 * - To handle requests for tariff data over a specified time interval.
 * - Intended for features like graphing tariffs by partner country over time.
 * 
 * TODO:
 * - Refine fields as needed based on future requirements.
 * - Add validation logic (e.g., ensure startDate is before endDate).
 * - Coordinate with the service and repository layers for implementation.
 */
public record TariffOverviewQueryDTO (
    String reportingCountry,
    String partnerCountry,
    String item, 
    Double itemCost,
    LocalDate startDate,
    LocalDate endDate
) {}
