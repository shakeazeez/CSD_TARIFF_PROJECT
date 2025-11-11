package com.tariff.calculation.tariffCalc.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.SelectedItemsDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffDetailsforItemDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffItemFilterDTO;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffDetails;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

@Service
public class BankIndustrySearchServiceImpl implements BankIndustrySearchService {

    private final ItemRepo itemRepo;
    private final TariffRepo tariffRepo;
    private final CountryRepo countryRepo;

    private final Logger log = LoggerFactory.getLogger(BankIndustrySearchServiceImpl.class);

    public BankIndustrySearchServiceImpl(ItemRepo itemRepo, TariffRepo tariffRepo, CountryRepo countryRepo) {
        this.itemRepo = itemRepo;
        this.tariffRepo = tariffRepo;
        this.countryRepo = countryRepo;
    }

    /**
     * Retrieves a list of item names available in the specified industry, filtered
     * by country and date range.
     *
     * @param itemFilterDTO DTO containing:
     *                      - industry: the industry to filter items by (must match
     *                      enum constant)
     *                      - homeCountry: the reporting country to filter tariffs
     *                      by
     *                      - startDate: the start date of the filter range (format
     *                      "yyyy-MM-dd")
     *                      - endDate: the end date of the filter range (format
     *                      "yyyy-MM-dd")
     * @return List of item names (String) that have at least one tariff record for
     *         the given country and date range.
     *         Returns an empty list if no items match.
     */
    public List<String> getAllItemsAvailableInTheIndustry(TariffItemFilterDTO itemFilterDTO) {
        log.info(itemFilterDTO.toString());
        int startYear = LocalDate.parse(itemFilterDTO.startDate()).getYear();
        int endYear = LocalDate.parse(itemFilterDTO.endDate()).getYear();

        String countryName = itemFilterDTO.homeCountry();
        Industry industry = Industry.valueOf(itemFilterDTO.industry());
        List<Item> preItemList = itemRepo.findByIndustry(industry);

        List<Item> filteredItemList = new ArrayList<>();

        log.info("Items to be printed" + preItemList.toString() + "\n\n\n");

        // items that users can select should at least have 1 entry of tariff details
        // for the date range
        for (Item item : preItemList) {
            Country reportingCountry = countryRepo.findByCountryName(countryName)
                    .orElseThrow(() -> new IllegalArgumentException("Country not found."));

            // log.info("Reporting country FOUND: " +reportingCountry.toString() +
            // "\n\n\n");

            List<Tariff> tariffList = tariffRepo.findByReportingCountryAndItem(reportingCountry, item);

            // log.info("tariffList" + tariffList.toString() + "\n\n\n");

            for (Tariff tariff : tariffList) {
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

    /**
     * Retrieves detailed tariff information for a list of selected items, filtered
     * by reporting country and date range.
     *
     * @param selectedItemsDTO DTO containing:
     *                         - selectedItems: array of item names to retrieve
     *                         details for
     *                         - homeCountry: the reporting country to filter
     *                         tariffs by
     *                         - industry: the industry of the items
     *                         - startDate: the start date of the filter range
     *                         (format "yyyy-MM-dd")
     *                         - endDate: the end date of the filter range (format
     *                         "yyyy-MM-dd")
     * @return TariffDetailsforItemDTO, each containing tariff details for the top
     *         10 lowest-tariff partner countries of the selected item.
     */

    public TariffDetailsforItemDTO getTariffDetailsForItem(SelectedItemsDTO selectedItemsDTO) {
        log.info(selectedItemsDTO.toString());
        
        List<TariffDetails> topTenCountriesTariffDetails = new ArrayList<>();

        String itemName = selectedItemsDTO.selectedItem();

        // Find the item object to get its hscode
        Country partnerCountry = countryRepo.findByCountryName(selectedItemsDTO.homeCountry())
                .orElseThrow(() -> new IllegalArgumentException("No home country"));
        // Find the reporting country as a country object
        Country reportingCountry = countryRepo.findByCountryName(selectedItemsDTO.homeCountry())
                .orElseThrow(() -> new IllegalArgumentException("Country not found."));
        Item item = itemRepo.findByItemNameAndCountry(itemName, partnerCountry)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));

        int hscode = item.getItemCode();

        // Set the start and end year to check tariffs against
        String startDateStr = selectedItemsDTO.startDate();
        String endDateStr = selectedItemsDTO.endDate();
        LocalDate startDate = startDateStr != null && !startDateStr.isEmpty() ? LocalDate.parse(startDateStr)
                : LocalDate.now().minusYears(100);
        LocalDate endDate = endDateStr != null && !endDateStr.isEmpty() ? LocalDate.parse(endDateStr) : LocalDate.now();
        int startYear = startDate.getYear();
        int endYear = endDate.getYear();

        // Map to store the list of tariffs that a partner country has for this item
        Map<Country, List<Tariff>> map = new HashMap<>();

        // List of partner countries to filter through
        List<Country> partnerCountryList = tariffRepo.findByReportingCountryAndItem(reportingCountry, item).stream()
                .map(tariff -> tariff.getPartnerCountry())
                .distinct()
                .collect(Collectors.toList());

        // Find all tariff entries for the partner countries
        for (Country country : partnerCountryList) {
            List<Tariff> partnerCountryTariffList = tariffRepo
                    .findByReportingCountryAndPartnerCountryAndItem(reportingCountry, country, item)
                    .stream()
                    .filter(tariff -> tariff.getLocalDate() != null)
                    .filter(tariff -> tariff.getLocalDate().getYear() >= startYear
                            && tariff.getLocalDate().getYear() <= endYear)
                    .filter(tariff -> tariff.getPercentageRate() > 0.0) // prevent from showing tariffs with 0
                                                                        // percentage
                    .collect(Collectors.toList());

            if (partnerCountryTariffList.isEmpty()) {
                continue;
            }

            map.putIfAbsent(country, partnerCountryTariffList);
        }

        // Filter and store the top ten countries with top ten lowest tariff rates
        List<Map.Entry<Country, List<Tariff>>> topTenList = filterForTopTenBestCountries(map);

        // Sort the top ten best partner countries by tariff date
        for (Map.Entry<Country, List<Tariff>> entry : topTenList) {
            List<Tariff> sortedTariffs = entry.getValue().stream()
                    .filter(t -> t.getLocalDate() != null)
                    .sorted(Comparator.comparing(Tariff::getLocalDate))
                    .toList();

            double averageTariffRate = getAverageTariffRate(sortedTariffs);

            TariffDetails tariff = new TariffDetails(entry.getKey(), sortedTariffs, averageTariffRate);
            topTenCountriesTariffDetails.add(tariff);
        }

        return new TariffDetailsforItemDTO(hscode, itemName, topTenCountriesTariffDetails);
    }

    private static List<Map.Entry<Country, List<Tariff>>> filterForTopTenBestCountries(Map<Country, List<Tariff>> map) {
        List<Map.Entry<Country, List<Tariff>>> topTenCountries = map.entrySet().stream()
                .sorted((e1, e2) -> {
                    Optional<Tariff> latest1 = e1.getValue().stream()
                            .max(Comparator.comparing(Tariff::getLocalDate));
                    Optional<Tariff> latest2 = e2.getValue().stream()
                            .max(Comparator.comparing(Tariff::getLocalDate));
                    double rate1 = latest1.map(Tariff::getPercentageRate).orElse(0.0);
                    double rate2 = latest2.map(Tariff::getPercentageRate).orElse(0.0);

                    int rateCompare = Double.compare(rate1, rate2);
                    if (rateCompare != 0) {
                        return rateCompare;
                    }

                    // If rates are equal, use country name as tiebreaker for consistent ordering
                    return e1.getKey().getCountryName().compareTo(e2.getKey().getCountryName());
                })
                .limit(10)
                .collect(Collectors.toList());

        return topTenCountries;
    }

    private static double getAverageTariffRate(List<Tariff> tariffs) {
        double averageRate = tariffs.stream()
                .mapToDouble(Tariff::getPercentageRate)
                .average()
                .orElse(0.0);

        return averageRate;
    }
}
