package com.tariff.calculation.tariffCalc.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewResponseDTO;
import com.tariff.calculation.tariffCalc.dto.HistoricalTariffData;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewQueryDTO;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        WitsDTO result = null;  
        try {
            result = restClientWits.get()
                                   .uri("datasource/TRN/reporter/" + reportingCountry.getCountryNumber()+ 
                                       "/partner/"+ partnerCountry.getCountryNumber() + 
                                       "/product/"  + item.getItemCode() + 
                                       "/year/all/datatype/reported?format=JSON")
                                   .retrieve()
                                   .onStatus((status) -> status.value() == 400, (request, response) -> {
                                       throw new IllegalArgumentException("Dont have for this specific combination");
                                   })
                                   .body(WitsDTO.class);
        } catch (IllegalArgumentException e) {
            log.info("Exception found at " + e.getMessage());
            result = restClientWits.get()
                                    .uri("datasource/TRN/reporter/" + reportingCountry.getCountryNumber()+ 
                                        "/partner/000/product/"  + item.getItemCode() + "/year/all/datatype/reported?format=JSON")
                                    .retrieve()
                                    .onStatus((stat) -> stat.value() == 400, (req2, res1) -> {
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
        Map<String, List<Object>> dataObservation = seriesMap.get("0:0:0:0:0").observations(); // "0:0:0:0:0" should be the only
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
            LocalDate startDate = LocalDateTime.parse(startDateString).toLocalDate();

            tariffs.add(new Tariff(reportingCountry, partnerCountry, item, tariffRate, startDate));
        }
        
        tariffs.forEach((tariff) -> tariffRepo.save(tariff));

        log.info("Successfully processed {} tariff observations from API", tariffs.size());
        return tariffs;
    }

    public TariffOverviewResponseDTO getTariffOverview(TariffOverviewQueryDTO queryDTO) {
        Country reportingCountry = countryRepo.findByCountryName(queryDTO.reportingCountry())
                .orElseThrow(() -> new IllegalArgumentException("Reporting country not found"));
                    
        Country partnerCountry = countryRepo.findByCountryName(queryDTO.partnerCountry())
                .orElseThrow(() -> new IllegalArgumentException("Partner country not found"));

        Item item = itemRepo.findByItemName(queryDTO.item())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        log.info("No problem with Item Query");

        // check if the tariffs are already in the database
        final List<Tariff> tariffList = tariffRepo.findByReportingCountryAndPartnerCountryAndItem(reportingCountry,
                partnerCountry, item);

        // if not, load from api
        if (tariffList.size() <= 1) {
            log.info("Attempting to load....");
            tariffList.addAll(loadTariffsFromApi(reportingCountry, partnerCountry, item));
        }

        List<HistoricalTariffData> historicalTariffData = tariffList.stream()
                .map(tariff -> new HistoricalTariffData(
                        tariff.getLocalDate(), // start period
                        tariff.getPercentageRate()))
                .sorted((a, b) -> a.startPeriod().compareTo(b.startPeriod())) // sort start period by date
                .toList();
        
        log.info("Returning {} historical tariff data points after filtering", historicalTariffData.size());

        return new TariffOverviewResponseDTO(
                reportingCountry.getCountryName(),
                partnerCountry.getCountryName(),
                historicalTariffData);
    }
    
    
    public List<Country> getAllCountries() {
        return countryRepo.findAll();
    }
}
