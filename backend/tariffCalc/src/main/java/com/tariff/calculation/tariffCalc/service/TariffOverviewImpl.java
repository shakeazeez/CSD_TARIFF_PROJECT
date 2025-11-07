package com.tariff.calculation.tariffCalc.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.GeneralTariffDTO;
import com.tariff.calculation.tariffCalc.dto.HistoricalTariffData;
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewResponseDTO;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.WitsDTO;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.dataSets.TariffDataSet;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.dataSets.TariffSeries;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.dataSets.TariffSeriesData;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure.Dimension;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure.Observation;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure.StartPeriod;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure.Structure;
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;
import com.tariff.calculation.tariffCalc.utility.LemmaUtils;

@Service
public class TariffOverviewImpl implements TariffOverviewService {

    private final TariffRepo tariffRepo;
    private final ItemRepo itemRepo;
    private final CountryRepo countryRepo;
    private final RestClient restClientWits;
    private final Logger log = LoggerFactory.getLogger(TariffOverviewImpl.class);

    public TariffOverviewImpl(
            CountryRepo countryRepo,
            ItemRepo itemRepo,
            TariffRepo tariffRepo,
            RestClient.Builder restClientBuilder) {
        this.countryRepo = countryRepo;
        this.itemRepo = itemRepo;
        this.tariffRepo = tariffRepo;
        this.restClientWits = restClientBuilder.clone()
                .baseUrl("https://wits.worldbank.org/API/V1/SDMX/V21/")
                .build();
    }

    // https://wits.worldbank.org/API/V1/SDMX/V21/datasource/TRN/reporter/840/partner/156/product/020110/year/all/datatype/reported?format=JSON
    private List<Tariff> loadTariffsFromApi(Country reportingCountry, Country partnerCountry, Item item)
            throws ApiFailureException {
        // log.info("7. inside loadTariffFromAPI\n");
        String reportingCountryNumber = Integer.toString(reportingCountry.getCountryNumber());
                
        while (reportingCountryNumber.length() < 3) {
            reportingCountryNumber = "0" + reportingCountryNumber;
        }

        String partnerCountryNumber = Integer.toString(partnerCountry.getCountryNumber());

        // log.info("7. reporting country number:{}\n", reportingCountryNumber);
                
        while (partnerCountryNumber.length() < 3) {
            partnerCountryNumber = "0" + partnerCountryNumber;
        }

        //  log.info("8. partner country number:{}\n", partnerCountryNumber);

        //  log.info("9. item.getItemCode() as integer: {}\n", item.getItemCode());
        // Integer itemCode = item.getItemCode();
        // log.info("10. is ItemCode an integer: {}", itemCode);

        // log.info(item.getItemCode().toString());

        String itemNum = String.format("%06d", item.getItemCode()); // since itemCode is stored as an integer, the leading zero gets cut off
        // String itemNum = Integer.toString(item.getItemCode());

       //  log.info("11. itemNum as a string, check if there's leading zero: " + itemNum);

        // log.info("itemNum: " + itemNum);

        // String itemNum = Integer.toString(item.getItemCode()).substring(0, 6);

        WitsDTO result = null;
        try {
            result = restClientWits.get()
                    .uri("datasource/TRN/reporter/" + reportingCountryNumber +
                            "/partner/" + partnerCountryNumber +
                            "/product/" + itemNum +
                            "/year/all/datatype/reported?format=JSON")
                    .retrieve()
                    .onStatus((status) -> status.value() == 400 || status.value() == 404, (request, response) -> {
                        throw new IllegalArgumentException("Dont have for this specific combination");
                    })
                    .body(WitsDTO.class);
        } catch (IllegalArgumentException e) {
            log.info("Exception found at " + e.getMessage());
            result = restClientWits.get()
                    .uri("datasource/TRN/reporter/" + reportingCountryNumber +
                            "/partner/000/product/" + itemNum + "/year/all/datatype/reported?format=JSON")
                    .retrieve()
                    .onStatus((stat) -> stat.value() == 400 || stat.value() == 404, (req2, res1) -> {
                        throw new ApiFailureException("Api call failed");
                    })
                    .body(WitsDTO.class);
        }

        if (result == null || result.dataSets() == null || result.structure() == null) {
            throw new ApiFailureException("Api call failed");
        }

        log.info("Successfully retrieved tariff data, processing observations...");

        // To get the dataObservation map:
        // String key is the index of date,
        // value is an array, the first element of the array contains the tariff rate
        List<Tariff> tariffs = new ArrayList<>();
        TariffDataSet dataSet = result.dataSets().get(0);
        TariffSeries series = dataSet.series();
        Map<String, TariffSeriesData> seriesMap = series.getSeriesData();
        Map<String, List<Object>> dataObservation = seriesMap.get("0:0:0:0:0").observations(); // "0:0:0:0:0" should be
                                                                                               // the only
                                                                                               // key in the series map

        // To get the dates, use the key from the dataObservation map
        // to map to the tariffStartDates
        Structure structure = result.structure();
        Dimension timeDimension = structure.dimensions();
        Observation timeObservation = timeDimension.observation().get(0);
        List<StartPeriod> tariffStartDates = timeObservation.values();

        // combine dataObservation with dates
        for (Map.Entry<String, List<Object>> entry : dataObservation.entrySet()) {
            // get the date index
            String key = entry.getKey(); // "0", "1", "2", ...
            int dateIndex = Integer.parseInt(key);

            // get the tariff rate
            List<Object> value = entry.getValue(); // [tariff rate, ...]
            Double tariffRate = Double.parseDouble(value.get(0) + "");

            // get the corresponding date
            String startDateString = tariffStartDates.get(dateIndex).start();
            log.info(startDateString);
            LocalDate startDate = LocalDateTime.parse(startDateString).toLocalDate();

            tariffs.add(new Tariff(reportingCountry, partnerCountry, item, tariffRate, "Info not available" ,startDate));
        }

        tariffs.forEach((tariff) -> tariffRepo.save(tariff));

        log.info("Successfully processed {} tariff observations from API", tariffs.size());
        return tariffs;
    }

    public TariffOverviewResponseDTO getTariffOverview(TariffCalculationQueryDTO queryDTO) {
        // log.info("1. === getTariffOverview called with: reporting={}, partner={}, item={} ===\n", 
             // queryDTO.reportingCountry(), queryDTO.partnerCountry(), queryDTO.item());
    
        Country reportingCountry = countryRepo.findByCountryName(queryDTO.reportingCountry())
                .orElseThrow(() -> new IllegalArgumentException("Reporting country not found"));

                // log.info("2. Found reporting country: {}\n", reportingCountry.getCountryName());

        Country partnerCountry = countryRepo.findByCountryName(queryDTO.partnerCountry())
                .orElseThrow(() -> new IllegalArgumentException("Partner country not found"));

                // log.info("3. Found partner country: {}\n", partnerCountry.getCountryName());

        Item item = itemRepo.findByItemNameAndCountry(LemmaUtils.toSingular(queryDTO.item().toLowerCase().trim()), reportingCountry)
                .orElseGet(() -> itemRepo.findByItemNameAndCountry(LemmaUtils.toSingular(queryDTO.item().trim()).toLowerCase(), 
                            countryRepo.findByCountryName("world").orElseThrow())
                .orElseThrow(() -> new IllegalArgumentException("Item not found for item " + queryDTO.item())));

        // log.info("4. Found item: {} with code: {}\n", item.getItemName(), item.getItemCode());

        log.info("No problem with Item Query");

        // check if the tariffs are already in the database
        final List<Tariff> tariffList = tariffRepo.findByReportingCountryAndPartnerCountryAndItem(reportingCountry,
                partnerCountry, item);

        // log.info("5. Found {} existing tariffs in database\n", tariffList.size());

        log.info(tariffList.toString());
        // if not, load from api
        if (tariffList.size() <= 1) {
            log.info("Attempting to load....");
            // log.info("6. Attempting to load from API because tariffList.size() = {}\n", tariffList.size());
            tariffList.addAll(loadTariffsFromApi(reportingCountry, partnerCountry, item));
        } 
        // else {
            // log.info("6. Using existing tariffs from database, skipping API call\n");
        // }

        List<HistoricalTariffData> historicalTariffData = tariffList.stream()
                .map(tariff -> new HistoricalTariffData(
                        tariff.getLocalDate(), // start period
                        tariff.getId(),
                        tariff.getPercentageRate(),
                        tariff.getPercentageRate() * queryDTO.itemCost() / 100.0,
                        queryDTO.itemCost() + tariff.getPercentageRate() * queryDTO.itemCost() / 100.0))
                .sorted((a, b) -> a.startPeriod().compareTo(b.startPeriod())) // sort start period by date
                .toList();

        log.info("Returning {} historical tariff data points after filtering", historicalTariffData.size());

        return new TariffOverviewResponseDTO(
                reportingCountry.getCountryName(),
                partnerCountry.getCountryName(),
                item.getItemName(),
                historicalTariffData);
    }

    public List<Country> getAllCountries() {
        return countryRepo.findAll();
    }
    
    public List<GeneralTariffDTO> getAllTariff(Integer tariffId) {
        Tariff tariff = tariffRepo.findById(tariffId)
                                  .orElseThrow(() -> new IllegalArgumentException("Unable to find tariff Id"));
                                  
        List<Tariff> tariffCopies = tariffRepo.findByReportingCountryAndPartnerCountryAndItem(
                                        tariff.getReportingCountry(),
                                        tariff.getPartnerCountry(),
                                        tariff.getItem()
                                    );
        List<GeneralTariffDTO> res = new ArrayList<>();
        
        tariffCopies.stream()
                    .sorted((a, b) -> a.getLocalDate().compareTo(b.getLocalDate())).forEach((tariffEntry) -> {
            res.add(new GeneralTariffDTO(tariffEntry.getReportingCountry().getCountryName()
                                ,tariffEntry.getPartnerCountry().getCountryName() 
                                ,tariffEntry.getItem().getItemName().replaceAll("[0-9]+", "").replaceAll("general", "")
                                ,tariffEntry.getPercentageRate()
                                ,tariffEntry.getDescription()));
        });
        
        return res;
    }
}
