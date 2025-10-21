package com.tariff.calculation.tariffCalc.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffItemFilterDTO;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.country.Country;


@Service
public class BankIndustrySearchServiceImpl implements BankIndustrySearchService {
    
    private final ItemRepo itemRepo;
    private final TariffRepo tariffRepo;

    public BankIndustrySearchServiceImpl (ItemRepo itemRepo, TariffRepo tariffRepo) {
        this.itemRepo = itemRepo;
        this.tariffRepo = tariffRepo;
    }

    /* 
     * Returns a list of items available in the industry (used for user filter)
     */
    public List<String> getAllItemsAvailableInTheIndustry(TariffItemFilterDTO itemFilterDTO) {

        int startYear = LocalDate.parse(itemFilterDTO.startDate()).getYear();
        int endYear = LocalDate.parse(itemFilterDTO.endDate()).getYear();

        Industry industry = Industry.valueOf(itemFilterDTO.industry());
        List<Item> preItemList = itemRepo.findByIndustry(industry); 

        List<Item> filteredItemList = new ArrayList<>();

        // items that users can select should at least have 1 entry of tariff details for the date range
        for (Item item: preItemList) {
            List<Tariff> tariffList = item.getTariffs();
            for (Tariff tariff: tariffList) {

                String reportingCountry = tariff.getReportingCountry().getCountryName();
                int yearReported = tariff.getLocalDate().getYear();

                boolean isWithinYearRange = startYear >= yearReported && yearReported <= endYear;
                boolean isSelectedCountry = reportingCountry.equals(itemFilterDTO.homeCountry());

                // at least need to have one tariff imposed on the item
                if (isSelectedCountry && isWithinYearRange) {
                    filteredItemList.add(item);
                    break;
                }
            }
        }

        return filteredItemList.stream().map(Item::getItemName).collect(Collectors.toList());
    }


}
