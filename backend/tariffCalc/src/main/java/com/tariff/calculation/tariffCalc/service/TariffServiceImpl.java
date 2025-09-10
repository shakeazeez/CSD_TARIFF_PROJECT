package com.tariff.calculation.tariffCalc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tariff.calculation.tariffCalc.country.CountryCode;
import com.tariff.calculation.tariffCalc.country.CountryCodesRepo;
import com.tariff.calculation.tariffCalc.dto.TariffQueryDTO;
import com.tariff.calculation.tariffCalc.dto.itemApiDto.ItemRetrievalDTO;
import com.tariff.calculation.tariffCalc.dto.tariffApiDto.MoachDTO;
import com.tariff.calculation.tariffCalc.dto.tariffApiDto.TableData;
import com.tariff.calculation.tariffCalc.dto.tariffApiDto.TariffRate;
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/*
 * Welcome to hell. This is my mystical garbage to call an API on the backend 
 * Since Java doesnt have an easy way to dynamically store an object, you have 
 * the mess known as DTO MoachQuery... 
 * 
 * And since we cant have simple thing on this earth, I used some interesting logic
 * to retrieve the tariff info out of the json response when we have to do external
 * api calls. 
 * 
 * @author: Joseph
*/
@Service
public class TariffServiceImpl implements TariffService {

    private final Logger log = LoggerFactory.getLogger(TariffServiceImpl.class);
    private final RestClient restClientMoach;
    private final RestClient restClientWits;
    private final CountryCodesRepo countryCodesRepo;
    private final ItemRepo itemRepo;
    private final TariffRepo tariffRepo;
    
    public TariffServiceImpl (
        CountryCodesRepo countryCodesRepo, 
        ItemRepo itemRepo, 
        TariffRepo tariffRepo,
        RestClient.Builder restClientBuilder
    ) {
        this.countryCodesRepo = countryCodesRepo;
        this.itemRepo = itemRepo;
        this.tariffRepo = tariffRepo;
        // TODO: The actual API please
        this.restClientMoach = restClientBuilder.clone()
                                                .baseUrl("")
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
     * TODO: The actual API please
     *
     * @param countryCode The reporting countriy's countryCodes 
     *                    that has been saved and loaded ahead of time 
     * @param item The item that has been requested 
     * @return a list of tariff objects with that particular item and
     *         that particular country code 
    */
    private List<Tariff> loadTariffFromApi(CountryCode countryCode, Item item) throws ApiFailureException {
        MoachDTO result = restClientMoach.get()
                                         .uri("")
                                         .retrieve()
                                         .onStatus((status) -> status.value() == 404, (request, response) -> {
                                             throw new IllegalArgumentException (response.getStatusText());
                                          })
                                         .body(MoachDTO.class);
        
        if (result == null || result.tariffData() == null) {
            throw new ApiFailureException("Unable to call api properly");
        }
        
        List<Tariff> res = new ArrayList<>();
        
        // sigh... This is gna be disgusting. Also, IDK why tf is there multiple data.... 
        // Actually i will leave this here for now since idt i know how I want to decipher this nonsence 
        // TODO: Which data we taking? 
        result.tariffData().stream()
                           .forEach((tariffData) -> {
                                TariffRate tariffRate = tariffData.tariffRate();
                                
                                // This is the US case where it is abit confusing since it stores general tariff 
                                // and seperate tariffs differently. This is why we dont try to be special 
                                if (tariffRate != null) {
                                    // countries
                                    String countriesInfo = tariffRate.countries();
                                    
                                    // This is the official 2 or 1 letter code provided to each country for some reason or another. 
                                    // Will be a database thing down the road
                                    List<String> countries = List.of(countriesInfo.substring(countriesInfo.indexOf('(') + 1, countriesInfo.indexOf(')'))
                                                                                  .split(","));
                                    String customRateInfo = countriesInfo.substring(countriesInfo.indexOf('('))
                                                                   .trim()
                                                                   .toLowerCase();
                                                                   
                                    // Stores rateInfo as decimal as it is a percentage initially
                                    if (customRateInfo.contains("%")) {
                                        customRateInfo = customRateInfo.substring(customRateInfo.indexOf('%'));
                                    }
    
                                    Double customRateValue =  customRateInfo.equals("free") ? 0 : Double.parseDouble(customRateInfo) / 100.0;
                                    countries.forEach((code) ->  {
                                        Optional<CountryCode> country = countryCodesRepo.findByCountryCode(code);
                                       
                                        if (!country.isEmpty()) {
                                            Tariff tariff = tariffRepo.save(new Tariff(countryCode, country.get(), item ,customRateValue));
                                            res.add(tariff);
                                        } 
                                    });
                                    
                                    // This is the world case 
                                    CountryCode world = countryCodesRepo.findByCountryName("World").get();
                                    
                                    String generalRateInfo = tariffRate.generalDutyRate().toLowerCase();
                                    Double generalRateValue = generalRateInfo != null && !generalRateInfo.equals("free") ? Double.parseDouble(generalRateInfo) : 0.0;
                                    
                                    Tariff tariff = tariffRepo.save(new Tariff(countryCode, world, item, generalRateValue));
                                    res.add(tariff);
                                }
                               
                                // This is the general case that all other tariff information seems to like to follow. 
                                // If by some reason this no longer applies for some edge case... Well bops
                                List<TableData> tariffInformation = tariffData.countryInformation();
                               
                                tariffInformation.forEach((information) -> {
                                    List<CountryCode> country = new ArrayList<>();
                                    
                                    Optional<CountryCode> firstCountry = countryCodesRepo.findByCountryName(information.tariffRegion());
                                    
                                    if (!firstCountry.isEmpty()) {
                                        country.add(firstCountry.get());
                                    }
                                    
                                    // The spliting is wrong. I need to look over this first. If you can 
                                    // help ping me for the sample queries 
                                    // TODO: Fix this garbage
                                    List<String> countryNames = List.of(information.country().split(","));
                                    
                                    countryNames.forEach((names) -> {
                                        Optional<CountryCode> temp = countryCodesRepo.findByCountryName(names);
                                        
                                        if (!temp.isEmpty()) {
                                            country.add(temp.get());
                                        }
                                    });
                                    
                                    // Sorry about this line bear with me. I will maybe clean this up
                                    String regionTariffRate = information.tariffRate().substring(information.tariffRate().indexOf("%"));
                                    Double regionTariffRateValue;
                                    if (regionTariffRate == null || "".equals(regionTariffRate) || "0.0".equals(regionTariffRate)){
                                        regionTariffRateValue = 0.0;
                                    } else {
                                        regionTariffRateValue = Double.parseDouble(regionTariffRate);
                                    }
                                    
                                    country.forEach((regionCountry) -> res.add(tariffRepo.save(new Tariff(countryCode, regionCountry, item, regionTariffRateValue))));
                                });
                               
                            });
        return res;
    }
    
    /*
     * This is to load the item code from an API. Will be stored within a database 
     * to conserve our amount of queries for the API. This also stores whateveer we 
     * get into our database with the item
     * 
     * TODO: The actual API please
     *
     * @Param Item name
     * returns Item object with Hscode 
    */
    private Item loadItemFromApi(String itemName) throws ApiFailureException {
        ItemRetrievalDTO result = restClientMoach.get()
                                         .uri("")
                                         .retrieve()
                                         .onStatus((status) -> status.value() == 404, (request, response) -> {
                                             throw new IllegalArgumentException (response.getStatusText());
                                          })
                                         .body(ItemRetrievalDTO.class); 
        if (result == null || result.codes() == null) {
            throw new ApiFailureException("Api call failed");
        }
        
        int itemCode = Integer.parseInt(result.codes().get(0).itemCode());
        return itemRepo.save(new Item(itemCode, itemName, new ArrayList<>()));
    }
    
    /*
    * This is the exposed service call for the endpoint when requesting REGULAR tariffs
    * 
    * @Param tariffQueryDTO. This is the information received from the frontend and will return the value of 
    *                        the tariff. 
    * @return                TODO: TBD whether should I just send the result or send every piece of information 
    */
    public Integer retrieveTariffPercentage(TariffQueryDTO tariffQueryDTO) {
        // This should already be statically loaded ahead of time 
        CountryCode reportingCode = countryCodesRepo.findByCountryName(tariffQueryDTO.reportingCountry())
                                                    .orElseThrow(() -> new IllegalArgumentException("Country not found"));
          
        // Checks for item. If not in database, query from the actual API                                       
        Item item = itemRepo.findByItemName(tariffQueryDTO.item())
                            .orElseGet(() -> loadItemFromApi(tariffQueryDTO.item()));
       
        List<Tariff> tariffList = tariffRepo.findByReportingCountryAndItem (
                                                        tariffQueryDTO.partnerCountry(), 
                                                        item);
                                  
        if (tariffList.isEmpty()) {
            tariffList = loadTariffFromApi(reportingCode, item);
        }
        
        Tariff tariff = tariffList.stream()
                                  .filter((tariffs) -> tariffs.getPartnerCountry().equals(tariffQueryDTO.partnerCountry()))
                                  .findFirst()
                                  .orElseThrow(() -> new IllegalArgumentException("Unable to find the tariff"));
        
                                  
        // TODO: Do the tariff calculation here 
        double percentage = tariff.getPercentageRate();
        return 0;
    }
}