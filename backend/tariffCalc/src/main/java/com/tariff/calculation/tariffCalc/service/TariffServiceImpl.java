package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.country.CountryCodesRepo;
import com.tariff.calculation.tariffCalc.dto.TariffQueryDTO;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.AllArgsConstructor;

@Service
public class TariffServiceImpl implements TariffService {
    
    private final RestClient restClientMoach;
    private final RestClient restClientWits;
    private final CountryCodesRepo countryCodeRepo;
    private final ItemRepo itemRepo;
    private final TariffRepo tariffRepo;
    
    public TariffServiceImpl(CountryCodesRepo countryCodesRepo, ItemRepo itemRepo, TariffRepo tariffRepo
        , RestClient.Builder restClientBuilder
    ) {
        this.countryCodeRepo = countryCodesRepo;
        this.itemRepo = itemRepo;
        this.tariffRepo = tariffRepo;
        this.restClientMoach = restClientBuilder.baseUrl("").build();
        this.restClientWits = restClientBuilder.baseUrl("").build();
    }
    
    public Integer retrieveTariffPercentage(TariffQueryDTO tariffQueryDTO) {
        
        
        
        return 0;
    }
}