package com.tariff.calculation.tariffCalc.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewQueryDTO;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.PastTariffResponseDTO;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.WitsDTO;
import com.tariff.calculation.tariffCalc.dto.itemApiDto.ItemRetrievalDTO;
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

@Service
public class TariffOverviewImpl implements TariffOverviewService {

    private final TariffRepo tariffRepo;
    private final ItemRepo itemRepo;
    private final CountryRepo countryRepo; // TOOD
    private final RestClient restClientWits; // TODO


    public TariffOverviewImpl (
        CountryRepo countryRepo,
        ItemRepo itemRepo,
        TariffRepo tariffRepo,
        RestClient.Builder restClientBuilder
    )   {
        this.countryRepo = countryRepo;
        this.itemRepo = itemRepo;
        this.tariffRepo = tariffRepo;
        this.restClientWits = restClientBuilder.clone()
                                                .baseUrl("")
                                                .build();

    }

    private Item loadItemFromApi(String itemName) throws ApiFailureException {
        ItemRetrievalDTO result = restClientWits.get()
                                         .uri("")
                                         .retrieve()
                                         .onStatus((status) -> status.value() == 404, (request, response) -> {
                                             throw new ApiFailureException (response.getStatusText());
                                          })
                                         .body(ItemRetrievalDTO.class); 

        if (result == null || result.codes() == null) {
            throw new ApiFailureException("Api call failed");
        }
        
        int itemCode = Integer.parseInt(result.codes().get(0).itemCode());
        return itemRepo.save(new Item(itemCode, itemName, new ArrayList<>()));
    }

    // TODO: extract tariff rates and start dates
    private List<Tariff> loadTariffFromApi(Country reportingCountry, Country partnerCountry, Item item) throws ApiFailureException {
        WitsDTO result = restClientWits.get()
                                       .uri("")
                                       .retrieve()
                                       .onStatus((status) -> status.value() == 404, (request, response) -> {
                                           throw new ApiFailureException (response.getStatusText());
                                        })
                                       .body(WitsDTO.class); 

        if (result == null || result.getData() == null) {
            throw new ApiFailureException("Api call failed");
        }

        List<Tariff> tariffs = new ArrayList<>();
        
    
        return tariffs;
    }
    
    public PastTariffResponseDTO getTariffOverview(TariffOverviewQueryDTO queryDTO) {
        Country reportingCountry = countryRepo.findByCountryName(queryDTO.reportingCountry())
                                                    .orElseThrow(() -> new IllegalArgumentException("Reporting country not found"));

        Country partnerCountry = countryRepo.findByCountryName(queryDTO.partnerCountry())
                                                    .orElseThrow(() -> new IllegalArgumentException("Partner country not found"));
                                             
        Item item = itemRepo.findByItemName(queryDTO.item())
                            .orElseGet(() -> loadItemFromApi(queryDTO.item()));
       
        final List<Tariff> tariffList = tariffRepo.findByReportingAndPartnerCountryAndItem(reportingCountry, partnerCountry, item);

        if (tariffList.isEmpty()) {
            tariffList.addAll(loadTariffFromApi(reportingCountry, partnerCountry, item));
        }

        // TODO

        return null; 
    }

}
