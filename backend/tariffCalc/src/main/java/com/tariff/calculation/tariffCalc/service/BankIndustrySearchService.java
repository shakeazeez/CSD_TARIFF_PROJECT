package com.tariff.calculation.tariffCalc.service;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.SelectedItemsDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffDetailsforItemDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffItemFilterDTO;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.category.Industry;

public interface BankIndustrySearchService {
    
    /**
     * Returns a list of item names available in the specified industry and date range, 
     * filtered by the country and tariff data.
     *
     * @param itemFilterDTO a DTO containing the filter criteria:
     *        - homeCountry: the country to filter tariffs by (e.g., "CHINA")
     *        - industry: the industry to search items in (must match the enum constant, e.g., "AGRICULTURE")
     *        - startDate: the start date of the period (format "yyyy-MM-dd")
     *        - endDate: the end date of the period (format "yyyy-MM-dd")
     *
     * @return a list of item names (String) that are available in the specified industry, country, and date range.
     *         If no items match, returns an empty list.
     */
    List<String> getAllItemsAvailableInTheIndustry(TariffItemFilterDTO itemFilterDTO);


    /*
     * Returns the TariffDetailsforItem - includes item name, HS code, list of TariffDetails (for each country)
     */

    List<TariffDetailsforItemDTO> getTariffDetailsForItems (SelectedItemsDTO selectedItemsDTO);

}
