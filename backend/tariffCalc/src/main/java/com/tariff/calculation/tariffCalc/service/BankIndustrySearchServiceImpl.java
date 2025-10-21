package com.tariff.calculation.tariffCalc.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffItemFilterDTO;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.service.TariffCalculationImpl;


@Service
public class BankIndustrySearchServiceImpl implements BankIndustrySearchService {
    
    private final ItemRepo itemRepo;
    private final TariffRepo tariffRepo;
    private final CountryRepo countryRepo;

    private final Logger log = LoggerFactory.getLogger(BankIndustrySearchServiceImpl.class);

    public BankIndustrySearchServiceImpl (ItemRepo itemRepo, TariffRepo tariffRepo, CountryRepo countryRepo) {
        this.itemRepo = itemRepo;
        this.tariffRepo = tariffRepo;
        this.countryRepo = countryRepo;
    }

    /* 
     * Returns a list of items available in the industry (used for user filter)
     */
    public List<String> getAllItemsAvailableInTheIndustry(TariffItemFilterDTO itemFilterDTO) {

        int startYear = LocalDate.parse(itemFilterDTO.startDate()).getYear();
        int endYear = LocalDate.parse(itemFilterDTO.endDate()).getYear();

        String countryName = itemFilterDTO.homeCountry();
        Industry industry = Industry.valueOf(itemFilterDTO.industry());
        List<Item> preItemList = itemRepo.findByIndustry(industry); 

        List<Item> filteredItemList = new ArrayList<>();

        // log.info("Items to be printed" + preItemList.toString());

        // items that users can select should at least have 1 entry of tariff details for the date range
        for (Item item: preItemList) {
            Country reportingCountry =  countryRepo.findByCountryName(countryName)
                                        .orElseThrow(() -> new IllegalArgumentException("Country not found."));

            // log.info("Reporting country FOUND: " +reportingCountry.toString());

            List<Tariff> tariffList = tariffRepo.findByReportingCountryAndItem(reportingCountry, item);

            // log.info("tariffList" + tariffList.toString() + "\n\n\n\n\n\n\n");

            for (Tariff tariff: tariffList) {
                int yearReported = tariff.getLocalDate().getYear();

                boolean isWithinYearRange = startYear <= yearReported && yearReported <= endYear;

                // at least need to have one tariff imposed on the item
                if (isWithinYearRange) {
                    filteredItemList.add(item);
                    break;
                }
            }
        }

        return filteredItemList.stream().map(Item::getItemName).collect(Collectors.toList());
    }


}
