package com.tariff.calculation.tariffCalc.service;

import java.util.ArrayList;
import java.util.function.DoubleFunction;

import com.tariff.calculation.tariffCalc.country.CountryCode;
import com.tariff.calculation.tariffCalc.country.CountryCodesRepo;
import com.tariff.calculation.tariffCalc.dto.itemApiDto.ItemRetrievalDTO;
import com.tariff.calculation.tariffCalc.dto.tariffApiDto.MoachDTO;
import com.tariff.calculation.tariffCalc.dto.tariffApiDto.TariffQueryDTO;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TariffServiceImpl implements TariffService {

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
        this.restClientMoach = restClientBuilder.clone()
                                                .baseUrl("")
                                                .build();
        this.restClientWits = restClientBuilder.clone()
                                               .baseUrl("")
                                               .build();
    }
    
    /*
     * @param
    */
    private Tariff loadTariffFromApi(CountryCode countryCode, Item item, String partnerCountry) {
        MoachDTO result = restClientMoach.get()
                                         .uri("")
                                         .retrieve()
                                         .onStatus((status) -> status.value() == 404, (request, response) -> {
                                             throw new IllegalArgumentException (response.getStatusText());
                                          })
                                         .body(MoachDTO.class);
        
        return null;
    }
    
    /*
     * @Param(Item name)
     * returns Item object with Hscode
     * This also stores whateveer we get into our database with the item 
    */
    private Item loadItemFromApi(String itemName) {
        ItemRetrievalDTO result = restClientMoach.get()
                                         .uri("")
                                         .retrieve()
                                         .onStatus((status) -> status.value() == 404, (request, response) -> {
                                             throw new IllegalArgumentException (response.getStatusText());
                                          })
                                         .body(ItemRetrievalDTO.class); 
        
        
        int itemCode = Integer.parseInt(result.codes().get(0).itemCode());
        Item returningItem = itemRepo.save(new Item(itemCode, itemName, new ArrayList<>()));
        return returningItem;
    }
    
    public Integer retrieveTariffPercentage(TariffQueryDTO tariffQueryDTO) {
        // This should already be statically loaded ahead of time 
        CountryCode reportingCode = countryCodesRepo.findByCountryName(tariffQueryDTO.reportingCountry())
                                                    .orElseThrow(() -> new IllegalArgumentException("Country not found"));
          
        // Checks for item. If not in database, query from the actual API                                       
        Item item = itemRepo.findByItemName(tariffQueryDTO.item())
                            .orElseGet(() -> loadItemFromApi(tariffQueryDTO.item()));
       
        Tariff tariff = tariffRepo.findByReportingCountryAndItemAndPartnerCountry (
                                                        tariffQueryDTO.partnerCountry(), 
                                                            item,
                                                            tariffQueryDTO.reportingCountry())
                                  .orElseGet(() -> loadTariffFromApi(reportingCode, item, tariffQueryDTO.partnerCountry()));
        
        double percentage = tariff.getPercentageRate();
        return 0;
    }
}