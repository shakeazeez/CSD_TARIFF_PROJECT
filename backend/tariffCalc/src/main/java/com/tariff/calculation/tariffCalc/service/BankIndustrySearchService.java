package com.tariff.calculation.tariffCalc.service;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffItemFilterDTO;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.category.Industry;

public interface BankIndustrySearchService {
    
    /* 
     * Returns a list of items available in the industry (for user select)
     */
    List<String> getAllItemsAvailableInTheIndustry(TariffItemFilterDTO itemFilterDTO);

    

}
