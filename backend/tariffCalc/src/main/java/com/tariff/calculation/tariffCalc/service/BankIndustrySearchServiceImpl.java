package com.tariff.calculation.tariffCalc.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.SelectedItemsDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffDetailsforItemDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffItemFilterDTO;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffDetails;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.country.Country;


@Service
public class BankIndustrySearchServiceImpl implements BankIndustrySearchService {
    
    private final ItemRepo itemRepo;
    private final TariffRepo tariffRepo;
    private final CountryRepo countryRepo;

    // private final Logger log = LoggerFactory.getLogger(BankIndustrySearchServiceImpl.class);

    public BankIndustrySearchServiceImpl (ItemRepo itemRepo, TariffRepo tariffRepo, CountryRepo countryRepo) {
        this.itemRepo = itemRepo;
        this.tariffRepo = tariffRepo;
        this.countryRepo = countryRepo;
    }

    /**
     * Retrieves a list of item names available in the specified industry, filtered by country and date range.
     *
     * @param itemFilterDTO DTO containing:
     *                      - industry: the industry to filter items by (must match enum constant)
     *                      - homeCountry: the reporting country to filter tariffs by
     *                      - startDate: the start date of the filter range (format "yyyy-MM-dd")
     *                      - endDate: the end date of the filter range (format "yyyy-MM-dd")
     * @return List of item names (String) that have at least one tariff record for the given country and date range.
     *         Returns an empty list if no items match.
     */
    public List<String> getAllItemsAvailableInTheIndustry(TariffItemFilterDTO itemFilterDTO) {

        int startYear = LocalDate.parse(itemFilterDTO.startDate()).getYear();
        int endYear = LocalDate.parse(itemFilterDTO.endDate()).getYear();

        String countryName = itemFilterDTO.homeCountry();
        Industry industry = Industry.valueOf(itemFilterDTO.industry());
        List<Item> preItemList = itemRepo.findByIndustry(industry); 

        List<Item> filteredItemList = new ArrayList<>();

        // log.info("Items to be printed" + preItemList.toString() + "\n\n\n");

        // items that users can select should at least have 1 entry of tariff details for the date range
        for (Item item: preItemList) {
            Country reportingCountry =  countryRepo.findByCountryName(countryName)
                                        .orElseThrow(() -> new IllegalArgumentException("Country not found."));

            // log.info("Reporting country FOUND: " +reportingCountry.toString() + "\n\n\n");

            List<Tariff> tariffList = tariffRepo.findByReportingCountryAndItem(reportingCountry, item);

            // log.info("tariffList" + tariffList.toString() + "\n\n\n");

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

    /**
     * Retrieves detailed tariff information for a list of selected items, filtered by reporting country and date range.
     *
     * @param selectedItemsDTO DTO containing:
     *        - selectedItems: array of item names to retrieve details for
     *        - homeCountry: the reporting country to filter tariffs by
     *        - industry: the industry of the items
     *        - startDate: the start date of the filter range (format "yyyy-MM-dd")
     *        - endDate: the end date of the filter range (format "yyyy-MM-dd")
     * @return List of TariffDetailsforItemDTO, each containing tariff details for the top 10 lowest-tariff partner countries per item.
     */

    public List<TariffDetailsforItemDTO> getTariffDetailsForItems (SelectedItemsDTO selectedItemsDTO) {

        List<TariffDetailsforItemDTO> result = new ArrayList<>();

        // List of selected items that we have to find tariff details about 
        List<Item> selectedItemList = Arrays.stream(selectedItemsDTO.selectedItems())
                .map(itemName -> itemRepo.findByItemName(itemName)
                .orElseThrow(() -> new IllegalArgumentException("Item not found.")))
                .collect(Collectors.toList());

        // Use to retrieve all tariffs using its reporting country and item 
        Country reportingCountry =  countryRepo.findByCountryName(selectedItemsDTO.homeCountry())
            .orElseThrow(() -> new IllegalArgumentException("Country not found."));

        // Use to check that the tariff falls within the range 
        int startYear = LocalDate.parse(selectedItemsDTO.startDate()).getYear();
        int endYear = LocalDate.parse(selectedItemsDTO.endDate()).getYear();    
            
        Map<Item, Map<Country, List<Tariff>>> map = new HashMap<>();

        // for each item -> for all partner countries -> get valid tariff details
        for (Item item: selectedItemList) {

            List<Country> partnerCountryList = tariffRepo.findByReportingCountryAndItem(reportingCountry, item).stream()
                .map(tariff -> tariff.getPartnerCountry())
                .distinct()
                .collect(Collectors.toList());

            for (Country country : partnerCountryList) {

                List<Tariff> partnerCountryTariffList = tariffRepo.findByReportingCountryAndPartnerCountryAndItem(reportingCountry, country, item)
                    .stream()
                    .filter(tariff -> tariff.getLocalDate().getYear() >= startYear && tariff.getLocalDate().getYear() <= endYear)
                    .collect(Collectors.toList());

                map.putIfAbsent(item, new HashMap<>());
                map.get(item).put(country, partnerCountryTariffList);
            }
        }

        // create tariffDetailsDTO for each item 
        for (Item item: map.keySet()) {

            List<TariffDetails> tariffDetailsList = new ArrayList<>();

            // from all the partnerCountries, filter out the top ten lowest tariff countries
            Map<Country, List<Tariff>> countryTariffs = map.get(item);
            List<Map.Entry<Country, List<Tariff>>> topTenCountries = countryTariffs.entrySet().stream()
                .sorted((e1, e2) -> {
                    Optional<Tariff> latest1 = e1.getValue().stream()
                        .max(Comparator.comparing(Tariff::getLocalDate));
                    Optional<Tariff> latest2 = e2.getValue().stream()
                        .max(Comparator.comparing(Tariff::getLocalDate));
                    double rate1 = latest1.map(Tariff::getPercentageRate).orElse(0.0);
                    double rate2 = latest2.map(Tariff::getPercentageRate).orElse(0.0);
                    return Double.compare(rate1, rate2); 
                })
            .limit(10)
            .collect(Collectors.toList());   

            // for each partner country in the top ten, calculate current and average rates
            for (Map.Entry<Country, List<Tariff>> entry : topTenCountries) {
                Country partnerCountry = entry.getKey();
                List<Tariff> tariffs = entry.getValue().stream()
                    .sorted(Comparator.comparing(Tariff::getLocalDate))
                    .toList();

                // get the current tariff rate
                // double currentRate = tariffs.stream()
                //         .max(Comparator.comparing(Tariff::getLocalDate))
                //         .map(Tariff::getPercentageRate)
                //         .orElse(0.0);

                double averageRate = tariffs.stream()
                        .mapToDouble(Tariff::getPercentageRate)
                        .average()
                        .orElse(0.0);

                // full tariff details needed for the partner country
                TariffDetails details = new TariffDetails(partnerCountry, tariffs, averageRate);
                tariffDetailsList.add(details);
        }

        // full tariff details needed for each item 
        TariffDetailsforItemDTO tariffDetailsDTO = new TariffDetailsforItemDTO(item.getItemCode(), item.getItemName(), tariffDetailsList);

        // add it to the list of tariffDetailsForItemDTO 
        result.add(tariffDetailsDTO);
        }

        return result;
    }
}
