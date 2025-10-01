package com.tariff.calculation.tariffCalc.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.GeneralTariffDTO;
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;
import com.tariff.calculation.tariffCalc.dto.itemApiDto.ItemRetrievalDTO;
import com.tariff.calculation.tariffCalc.dto.currentTariffApiDto.MoachDTO;
import com.tariff.calculation.tariffCalc.dto.currentTariffApiDto.TableData;
import com.tariff.calculation.tariffCalc.dto.currentTariffApiDto.TariffData;
import com.tariff.calculation.tariffCalc.dto.currentTariffApiDto.TariffRate;
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;
import com.tariff.calculation.tariffCalc.utility.LemmaUtils;

import org.hibernate.mapping.Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/*
 * This class is the service class that handles endpoints related to tariffs
 * Since Java doesnt have an easy way to dynamically store an object, you have
 * alot of DTOs elsewhere...
 *
 * It should only have 2 methods (working progress). One method to call
 * current api and one method to call historical API.
 *
 *  Currently completed:
 *
 * @author: Joseph, jing xi, shin en
*/
@Service
public class TariffCalculationImpl implements TariffCalculationService {

    private final Logger log = LoggerFactory.getLogger(TariffCalculationImpl.class);

    private final RestClient restClientMoach;
    private final CountryRepo countryRepo;
    private final ItemRepo itemRepo;
    private final TariffRepo tariffRepo;
    private final List<Integer> customValid = List.of(96, 156, 918, 356, 360, 392, 410, 458, 104, 586, 608, 702, 158,
            764, 840, 704, 784);

    public TariffCalculationImpl(
            CountryRepo countryRepo,
            ItemRepo itemRepo,
            TariffRepo tariffRepo,
            RestClient.Builder restClientBuilder) {
        this.countryRepo = countryRepo;
        this.itemRepo = itemRepo;
        this.tariffRepo = tariffRepo;
        this.restClientMoach = restClientBuilder.clone()
                .baseUrl("https://mtech-api.com/client/api")
                .build();
    }

    /*
     * This is to load every partner country's tariff info from the API.
     * Some will be called world to save on overall database safe since
     * there is a general tariff to everyone. This will also store data
     * into the database for reducing amount of queries
     *
     *
     * @param countryCode The reporting countriy's countryCodes
     * that has been saved and loaded ahead of time
     *
     * @param item The item that has been requested
     *
     * @return a list of tariff objects with that particular item and
     * that particular country code
     */
    public List<Tariff> loadTariffFromApi(Country countryCode, Item item) throws ApiFailureException {
        
        String countryNumber = Integer.toString(countryCode.getCountryNumber());
        
        while (countryNumber.length() < 3) {
            countryNumber = "0" + countryNumber;
        }
        
        MoachDTO result = restClientMoach.get()
                .uri("/tariff-data?product=" + item.getItemCode() + "&destination=" + countryNumber
                        + "&token=" + LemmaUtils.getEnvOrDotenv("MOACH_API_KEY"))
                .retrieve()
                .onStatus((status) -> status.value() == 400 || status.value() == 404, (request, response) -> {
                    // This one occurs if that country doesnt trade that item......
                    log.info("Api not found");
                    throw new ApiFailureException(response.getStatusText());
                })
                .body(MoachDTO.class);
        log.info("The result: " + result);
        if (result == null || result.tariffData() == null) {
            throw new ApiFailureException("Unable to call api properly");
        }

        log.info("Query results" + result.toString());
        List<Tariff> res = new ArrayList<>();

        // sigh... This is gna be disgusting. Also, IDK why is there multiple data....
        // log.info(result.tariffData().toString());
        TariffData tariffData = result.tariffData().get(0);
        TariffRate tariffRate = tariffData.getTariffRate();

        // This is the US case where it is abit confusing since it stores general tariff
        // and seperate tariffs differently. This is why we dont try to be special
        if (tariffRate != null) {
            String countriesInfo = tariffRate.countries();

            // Cheaper rate
            List<String> countries; 
            try {
                countries = List
                            .of(countriesInfo.substring(countriesInfo.indexOf('(') + 1, countriesInfo.indexOf(')'))
                                    .split(","));
                
            } catch (StringIndexOutOfBoundsException e) {
                countries = new ArrayList<>();
            }
            
            log.info(countries.toString());
            
            String customRateInfo ;
            try {
                customRateInfo = countriesInfo.substring(countriesInfo.indexOf('('))
                        .trim()
                        .toLowerCase();                
            } catch (StringIndexOutOfBoundsException e) {
                customRateInfo = "0.0";
            }

            // Stores rateInfo as decimal as it is a percentage initially
            if (customRateInfo.contains("%")) {
                customRateInfo = customRateInfo.substring(0, customRateInfo.indexOf('%'));
            }

            Double customRateValue = customRateInfo.equals("free") ? 0 : Double.parseDouble(customRateInfo) / 100.0;
            // log.info("Attempting to finding by Code");
            log.info(countries.toString());
            countries.forEach((code) -> {
                Optional<Country> country = countryRepo.findByCountryCode(code);
                log.info("For coding: " + code);
                if (country.isPresent()) {
                    Tariff tariff = tariffRepo
                            .save(new Tariff(countryCode, country.get(), item, customRateValue, "Special Rate of Duty",
                                    LocalDate.now()));
                    res.add(tariff);
                }
            });

            // log.info("No issue finding by Code");
            // test
            // This is the world case
            Country world = countryRepo.findByCountryName("world").get();
            String generalRateInfo = tariffRate.generalDutyRate().toLowerCase();
            Double generalRateValue = generalRateInfo != null && !generalRateInfo.equals("free")
                    ? Double.parseDouble(generalRateInfo)
                    : 0.0;

            Tariff tariff = tariffRepo.save(
                    new Tariff(countryCode, world, item, generalRateValue, "General Rate of Duty", LocalDate.now()));
            res.add(tariff);
        }

        // This is the general case that all other tariff information seems to like to
        // follow.
        // If by some reason this no longer applies for some edge case... Well bops
        List<TableData> tariffInformation = tariffData.getCountryInformation();
        log.info("The information " + tariffInformation.toString());
        tariffInformation.forEach((information) -> {
            List<Country> country = new ArrayList<>();
            if (information.getTariffRegion().contains("MFN")) {
                country.add(countryRepo.findByCountryName("world").get());
            } else if ("LDCs Preferential Tariff".equals(information.getTariffRegion())) {
                country.add(countryRepo.findByCountryName("developing").get());
            } else {
                log.info("The country" + information.getTariffRegion().trim());
                Optional<Country> firstCountry = countryRepo.findFirstByCountryNameContainingIgnoreCase(information.getTariffRegion().trim());
                if (!firstCountry.isEmpty()) {
                    country.add(firstCountry.get());
                }
            }
            // This is wrong because it is not standard how these countries are
            // described.....
            // help ping me for the sample queries
            List<String> countryNames = List.of(information.getCountry()
                    .replaceAll("ASEAN:", "")
                    .trim()
                    .split(","));

            log.info(countryNames.toString());

            countryNames.forEach((names) -> {
                log.info("For name: " + names);
                Optional<Country> temp = countryRepo.findByCountryName(names.trim());
                if (temp.isPresent() && !country.contains(temp.get())) {
                    country.add(temp.get());
                }
            });

            // log.info("No errors for parcing country names in the .country() part ");
            String preProcessed = information.getTariffRate().trim();
            log.info("Tariff before anything : " + preProcessed);
            String regionTariffRate = preProcessed.contains("%") ? preProcessed.substring(0, preProcessed.indexOf('%'))
                    : "";

            // log.info("Region tariff rate : " + regionTariffRate);
            double regionTariffRateValue = (regionTariffRate == null || "".equals(regionTariffRate)) ? 0.0
                    : Double.parseDouble(regionTariffRate);
            // log.info("No problem with Tariff Loading" + regionTariffRateValue);
            // log.info("No problem with Tariff Saving");
            log.info("Countries: " + country);
            country.forEach((regionCountry) -> {
                log.info("For regCountry: " + regionCountry);
                if (!customContains(res, countryCode, regionCountry)) {
                    res.add(tariffRepo.save(
                            new Tariff(countryCode, regionCountry, item, regionTariffRateValue,
                                    information.getTariffRegion() + " " + information.getCountry(), LocalDate.now())));
                }
            });
        });

        // This infrastructure must be changed

        return res;
    }

    private boolean customContains(List<Tariff> list, Country countryCode, Country regionCountry) {
        Optional<Tariff> temp = list.stream()
                .filter(tariff -> tariff.getReportingCountry().equals(countryCode)
                        && tariff.getPartnerCountry().equals(regionCountry))
                .findFirst();

        return temp.isPresent();
    }

    /*
     * This is to load the item code from an API. Will be stored within a database
     * to conserve our amount of queries for the API. This also stores whateveer we
     * get into our database with the item
     *
     *
     * @Param Item name
     * returns Item object with Hscode
     */
    // https://mtech-api.com/client/api/hs-code-match?q=tennis+shoes&category=156&token=YOUR_API_TOKEN
    public Item loadItemFromApi(String itemName, String countryNumber) throws ApiFailureException {

        ItemRetrievalDTO result;
        boolean general = countryNumber.equals("wto");
        result = restClientMoach.get()
                .uri("/hs-code-match?q=" + itemName + "&category=" + countryNumber + "&token="
                        + LemmaUtils.getEnvOrDotenv("MOACH_API_KEY"))
                .retrieve()
                .onStatus((status) -> status.value() == 404 || status.value() == 400, (request, response) -> {
                    throw new ApiFailureException(response.getStatusText());
                })
                .body(ItemRetrievalDTO.class);

        log.info("Query results" + result.toString());
        if (result == null || result.data() == null) {
            throw new ApiFailureException("Api call failed");
        }

        int itemCode = Integer.parseInt(result.data().codes().get(0).itemCode());
        Optional<Item> checker = itemRepo.findById(itemCode);

        if (checker.isPresent() && checker.get().getItemName().contains("general")) {
            return checker.get();
        }

        itemName = itemName + (general ? "general" : countryNumber);
        return itemRepo.save(new Item(itemCode, itemName, new ArrayList<>()));

    }

    /*
     * This is the exposed service call for the endpoint when requesting REGULAR
     * tariffs
     *
     * @Param tariffQueryDTO. This is the information received from the frontend and
     * will return the value of
     * the tariff.
     *
     * @return A TariffResponseDTO containing raw data of reporting country, partner
     * country,
     * item, tariff percentage, tariff cost, total cost post tariff
     */
    public TariffResponseDTO getCurrentTariffDetails(TariffCalculationQueryDTO tariffQueryDTO) {
        // This should already be statically loaded ahead of time
        log.info(tariffQueryDTO.toString());
        Country reportingCountry = countryRepo.findByCountryName(tariffQueryDTO.reportingCountry())
                .orElseThrow(() -> new IllegalArgumentException("Country not found"));
        Country partnerCountry = countryRepo.findByCountryName(tariffQueryDTO.partnerCountry())
                .orElseThrow(() -> new IllegalArgumentException("Country not found"));
        // Checks for item. If not in database, query from the actual API
        Item item;

        if (customValid.contains(reportingCountry.getCountryNumber())) {
            item = itemRepo
                    .findByItemName(LemmaUtils.toSingular(tariffQueryDTO.item()).toLowerCase().replaceAll(",", "")
                            + reportingCountry.getCountryNumber())
                    .orElseGet(() -> loadItemFromApi(LemmaUtils.toSingular(tariffQueryDTO.item().toLowerCase().replaceAll(",", "")),
                            Integer.toString(reportingCountry.getCountryNumber())));
        } else {
            item = itemRepo.findByItemName(LemmaUtils.toSingular(tariffQueryDTO.item()).toLowerCase() + "general")
                    .orElseGet(
                            () -> loadItemFromApi(LemmaUtils.toSingular(tariffQueryDTO.item().toLowerCase()), "wto"));
        }

        log.info("No problem with Item Query");
        // Needs to be final here because being used in a very interesting lambda later
        // down the line
        final List<Tariff> tariffList = tariffRepo.findByReportingCountryAndItem(
                reportingCountry,
                item);

        if (tariffList.isEmpty()) {
            log.info("Attempting to load....");
            tariffList.addAll(loadTariffFromApi(reportingCountry, item));
        }
        
        log.info(tariffList.toString());

        Tariff tariff = tariffList.stream()
                .filter((tariffs) -> tariffs.getPartnerCountry().getCountryNumber() == (partnerCountry.getCountryNumber()))
                .sorted((a, b) -> b.getLocalDate().compareTo(a.getLocalDate()))
                .findFirst()
                // Here, we check to return either developing tariff or non-developed tariff
                .orElseGet(() -> {
                    Country developing = countryRepo.findByCountryName("developing")
                            .orElseThrow(() -> {
                                log.info("Developing not found");
                                return new NoSuchElementException("Developing not found");
                            });
                    List<Tariff> developingTariff = tariffRepo
                            .findByReportingCountryAndPartnerCountryAndItem(developing, reportingCountry, item);
                    Country world = countryRepo.findByCountryName("world")
                            .orElseThrow(() -> {
                                log.info("World not found");
                                return new NoSuchElementException("World not found");
                            });

                    if (!developingTariff.isEmpty() && partnerCountry.getIsDeveloping()) {
                        return tariffList.stream()
                                .filter((currTariff) -> currTariff.getPartnerCountry().equals(developing))
                                .findFirst()
                                .orElseGet(() -> {
                                    log.info("Well for Developing");
                                    return tariffRepo.save(
                                            new Tariff(reportingCountry, developing, item, -1.0,
                                                    "No trade agreement found", LocalDate.now()));
                                });
                    }
                    return tariffList.stream()
                            .filter((currTariff) -> currTariff.getPartnerCountry().equals(world))
                            .findFirst()
                            .orElseGet(() -> {
                                log.info("Well for world");
                                return tariffRepo
                                        .save(new Tariff(reportingCountry, world, item, -1.0,
                                                "No trade agreement found", LocalDate.now()));
                            });
                });

        double percentage = tariff.getPercentageRate();
        double tariffAmount = percentage * tariffQueryDTO.itemCost() / 100.0;
        double itemCostWithTariff = tariffAmount + tariffQueryDTO.itemCost();
        return new TariffResponseDTO(reportingCountry.getCountryName(), tariffQueryDTO.partnerCountry(),
                item.getItemName().replaceAll("[0-9]+", "").replaceAll("general", ""), percentage, tariffAmount,
                itemCostWithTariff, tariff.getId(), tariff.getDescription());
    }

    public GeneralTariffDTO getTariffById(Integer tariffId) {
        Tariff tariff = tariffRepo.findById(tariffId)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find tariff Id"));

        return new GeneralTariffDTO(tariff.getReportingCountry().getCountryName(),
                tariff.getPartnerCountry().getCountryName(),
                tariff.getItem().getItemName().replaceAll("[0-9]+", "").replaceAll("general", ""),
                tariff.getPercentageRate(),
                tariff.getDescription());
    }

}
