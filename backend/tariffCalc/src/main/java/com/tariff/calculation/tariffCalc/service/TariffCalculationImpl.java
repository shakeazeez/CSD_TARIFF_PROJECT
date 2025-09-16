package com.tariff.calculation.tariffCalc.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import io.github.cdimascio.dotenv.Dotenv;

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
    private final RestClient restClientWits;
    private final CountryRepo countryRepo;
    private final ItemRepo itemRepo;
    private final TariffRepo tariffRepo;
    private final Dotenv dotenv = Dotenv.load();
    
    public TariffCalculationImpl (
        CountryRepo countryRepo, 
        ItemRepo itemRepo, 
        TariffRepo tariffRepo,
        RestClient.Builder restClientBuilder
    ) {
        this.countryRepo = countryRepo;
        this.itemRepo = itemRepo;
        this.tariffRepo = tariffRepo;
        this.restClientMoach = restClientBuilder.clone()
                                                .baseUrl("https://mtech-api.com/client/api")
                                                .build();
        this.restClientWits = restClientBuilder.clone()
                                               .baseUrl("")
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
     *                    that has been saved and loaded ahead of time 
     * @param item The item that has been requested 
     * @return a list of tariff objects with that particular item and
     *         that particular country code 
    */
    public List<Tariff> loadTariffFromApi(Country countryCode, Item item) throws ApiFailureException {
        // log.info("Entered function");
        MoachDTO result = restClientMoach.get()
                                         .uri("/tariff-data?product=" + item.getItemCode() + "&destination=" + countryCode.getCountryNumber() + "&token=" + dotenv.get("MOACH_API_KEY"))
                                         .retrieve()
                                         .onStatus((status) -> status.value() == 404, (request, response) -> {
                                             throw new ApiFailureException (response.getStatusText());
                                          })
                                         .body(MoachDTO.class);
        
        if (result == null || result.tariffData() == null) {
            throw new ApiFailureException("Unable to call api properly");
        }
        
        // log.info("Query results" + result.toString());
        List<Tariff> res = new ArrayList<>();
        
        // sigh... This is gna be disgusting. Also, IDK why is there multiple data.... 
        // Actually i will leave this here for now since idt i know how I want to decipher this nonsence 
        TariffData tariffData = result.tariffData().get(0);
        
        TariffRate tariffRate = tariffData.tariffRate();
                                
        // This is the US case where it is abit confusing since it stores general tariff 
        // and seperate tariffs differently. This is why we dont try to be special 
        if (tariffRate != null) {
            String countriesInfo = tariffRate.countries();
                                    
            // This is the official 2 or 1 letter code provided to each country for some reason or another. 
            // Will be a database thing down the road
            List<String> countries = List.of(countriesInfo.substring(countriesInfo.indexOf('(') + 1, countriesInfo.indexOf(')'))
                                                          .split(","));
            log.info(countries.toString());
            String customRateInfo = countriesInfo.substring(countriesInfo.indexOf('('))
                                                 .trim()
                                                 .toLowerCase();
                                                                   
            // Stores rateInfo as decimal as it is a percentage initially
            if (customRateInfo.contains("%")) {
                customRateInfo = customRateInfo.substring(0, customRateInfo.indexOf('%'));
            }
    
            Double customRateValue =  customRateInfo.equals("free") ? 0 : Double.parseDouble(customRateInfo) / 100.0;
            // log.info("Attempting to finding by Code");
            countries.forEach((code) ->  {
                Optional<Country> country = countryRepo.findByCountryCode(code);
                                        
                if (country.isPresent()) {
                    Tariff tariff = tariffRepo.save(new Tariff(countryCode, country.get(), item ,customRateValue, LocalDate.now()));
                    res.add(tariff);
                } 
            });
            // log.info("No issue finding by Code");
            // test
            // This is the world case 
            Country world = countryRepo.findByCountryName("world").get();
            // log.info("No issue finding world");
            String generalRateInfo = tariffRate.generalDutyRate().toLowerCase();
            Double generalRateValue = generalRateInfo != null && !generalRateInfo.equals("free") ? Double.parseDouble(generalRateInfo) : 0.0;
                                    
            Tariff tariff = tariffRepo.save(new Tariff(countryCode, world, item, generalRateValue, LocalDate.now()));
            res.add(tariff);
        }
                               
        // This is the general case that all other tariff information seems to like to follow. 
        // If by some reason this no longer applies for some edge case... Well bops
        List<TableData> tariffInformation = tariffData.countryInformation();
                                
        List<Country> country = new ArrayList<>();
        
        
        tariffInformation.forEach((information) -> {
            if ("MFN".equals(information.tariffRegion())) {
                log.info("Adding world");
                country.add(countryRepo.findByCountryName("world").get());
            } else if ("LDCs Preferential Tariff".equals(information.tariffRegion())) {
                log.info("Adding devloping");
                country.add(countryRepo.findByCountryName("developing").get());
            } else {
                Optional<Country> firstCountry = countryRepo.findByCountryName(information.tariffRegion());
                if (!firstCountry.isEmpty()) {
                    // log.info("Hehe haha");
                    country.add(firstCountry.get());
                } 
                // else {
                //     log.info("Husten we have a problem");
                // }
            }
                    
            // This is wrong because it is not standard how these countries are described.....
            // help ping me for the sample queries 
            // TODO: Fix this garbage
            List<String> countryNames = List.of(information.country().split(","));
            log.info(countryNames.toString());    

            countryNames.forEach((names) -> {
                // log.info(names);
                Optional<Country> temp = countryRepo.findByCountryName(names);
                        
                if (temp.isPresent()) {
                    // log.info("Found country for " + temp.get());
                    country.add(temp.get());
                } 
                // else {
                //     log.info("Unable to find the country named" + names);
                // }
            });
                
            countryNames.forEach((names) -> {
                // log.info(names);
                Optional<Country> temp = countryRepo.findByCountryName(names);
                        
                if (temp.isPresent()) {
                    // log.info("Found country for " + temp.get());
                    country.add(temp.get());
                } 
                // else {
                //     log.info("Unable to find the country named" + names);
                // }
             });

                
            // log.info("No errors for parcing country names in the .country() part ");
                                        
            // Sorry about this line bear with me. I will maybe clean this up
            String preProcessed = information.tariffRate();
            // log.info("Tariff before anything : " + preProcessed);
                
            String regionTariffRate = preProcessed.contains("%") ? preProcessed.substring(0, preProcessed.indexOf('%')) : "";
                
            // log.info("Region tariff rate : " + regionTariffRate);
            double regionTariffRateValue = (regionTariffRate == null || "".equals(regionTariffRate)) ? 0.0 : Double.parseDouble(regionTariffRate);
            // log.info("No problem with Tariff Loading" + regionTariffRateValue);                     
            country.forEach((regionCountry) -> {
                res.add(tariffRepo.save(new Tariff(countryCode, regionCountry, item, regionTariffRateValue, LocalDate.now())));
            });
            // log.info("No problem with Tariff Saving");                     
        });
        
        
        return res;
    }
    
    /*
     * This is to load the item code from an API. Will be stored within a database 
     * to conserve our amount of queries for the API. This also stores whateveer we 
     * get into our database with the item
     * 
     * TODO: Time to test :-) 
     *
     * @Param Item name
     * returns Item object with Hscode 
    */
    // https://mtech-api.com/client/api/hs-code-match?q=tennis+shoes&category=156&token=YOUR_API_TOKEN
    public Item loadItemFromApi(String itemName) throws ApiFailureException {
        ItemRetrievalDTO result = restClientMoach.get()
                                         .uri("/hs-code-match?q=" + itemName + "&category=wto&token=" + dotenv.get("MOACH_API_KEY"))
                                         .retrieve()
                                         .onStatus((status) -> status.value() == 404, (request, response) -> {
                                             throw new ApiFailureException (response.getStatusText());
                                          })
                                         // TODO:  Edge case of running out of api keys to be settled on a different day
                                         // .onStatus((status) -> status.value() == 402, (request, response) -> {
                                         //     return;
                                         // })
                                         .body(ItemRetrievalDTO.class); 
        
                                         
        log.info("Query results" + result.toString());
        if (result == null || result.data() == null) {
            throw new ApiFailureException("Api call failed");
        }
        
        int itemCode = Integer.parseInt(result.data().codes().get(0).itemCode());
        return itemRepo.save(new Item(itemCode, itemName, new ArrayList<>()));
        
    }
    
    /*
    * This is the exposed service call for the endpoint when requesting REGULAR tariffs
    * 
    * @Param tariffQueryDTO. This is the information received from the frontend and will return the value of 
    *                        the tariff. 
    * @return                TODO: TBD whether should I just send the result or send every piece of information 
    */
    public TariffResponseDTO getCurrentTariffDetails(TariffCalculationQueryDTO tariffQueryDTO) {
        // This should already be statically loaded ahead of time 
        Country reportingCountry = countryRepo.findByCountryName(tariffQueryDTO.reportingCountry())
                                                    .orElseThrow(() -> new IllegalArgumentException("Country not found"));
          
        // Checks for item. If not in database, query from the actual API                                       
        Item item = itemRepo.findByItemName(tariffQueryDTO.item())
                            .orElseGet(() -> loadItemFromApi(tariffQueryDTO.item().toLowerCase()));
       
        log.info("No problem with Item Query");
        // Needs to be final here because being used in a very interesting lambda later down the line 
        final List<Tariff> tariffList = tariffRepo.findByReportingCountryAndItem (
                                                        reportingCountry, 
                                                        item);
                                  
        if (tariffList.isEmpty()) {
            log.info("Attempting to load....");
            tariffList.addAll(loadTariffFromApi(reportingCountry, item));
        }
        
        log.info("I have escaped tariff hell");
        
        Tariff tariff = tariffList.stream()
                                  .filter((tariffs) -> tariffs.getPartnerCountry().getCountryName().equals(tariffQueryDTO.partnerCountry()))
                                  .findFirst()
                                  // Here, we check to return either developing tariff or non-developed tariff 
                                  .orElseGet(() -> {
                                        Country temp = countryRepo.findByCountryName(tariffQueryDTO.reportingCountry())
                                                                         .orElseThrow(() -> {
                                                                             log.info("Country not found");
                                                                             return new NoSuchElementException ("World not found");
                                                                         });
                                                                         
                                        Optional<Country> developing = countryRepo.findByCountryName("developing");
                                        Country world = countryRepo.findByCountryName("world")
                                                                            .orElseThrow(() -> {
                                                                                log.info("World not found");
                                                                                return new NoSuchElementException ("World not found");
                                                                            });
                                        if (temp.getIsDeveloping() && developing.isPresent()) {
                                            return tariffList.stream()
                                            // 
                                                             .filter((currTariff) -> currTariff.getPartnerCountry().equals(developing.get()))
                                                             .findFirst()
                                                             .orElseGet(() -> {
                                                                 log.info("Well for Developing");
                                                                 return tariffRepo.save(new Tariff(reportingCountry, developing.get(), item, -1.0, LocalDate.now()));
                                                             });
                                        } else {
                                            return tariffList.stream()
                                                             .filter((currTariff) -> currTariff.getPartnerCountry().equals(world))
                                                             .findFirst()
                                                             .orElseGet(() -> {
                                                                 log.info("Well for world");
                                                                 return tariffRepo.save(new Tariff(reportingCountry, world, item, -1.0, LocalDate.now()));
                                                             });
                                        }
                                });
                                
                                  // Havent decided about this yet actually....
                                  // .orElseThrow(() -> new IllegalArgumentException("Unable to find the tariff"));
        
        double percentage = tariff.getPercentageRate();
        double tariffAmount = percentage * tariffQueryDTO.itemCost();
        double itemCostWithTariff = tariffAmount + tariffQueryDTO.itemCost();
        return new TariffResponseDTO(reportingCountry.getCountryName(), tariffQueryDTO.partnerCountry(), item.getItemName(), percentage, tariffAmount, itemCostWithTariff);
    }
    
    
    public TariffResponseDTO getPastTariffDetails(TariffCalculationQueryDTO tariffQueryDTO) throws IllegalArgumentException, NoSuchElementException {
        // TODO: Add implementation for historical tariffs, remove exceptions thrown on signature
        return null;
    }
    
    
    public List<Tariff> getAllTariffInDatabase() {
        return tariffRepo.findAll();
    }
}