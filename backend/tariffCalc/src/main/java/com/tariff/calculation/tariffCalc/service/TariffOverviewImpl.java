package com.tariff.calculation.tariffCalc.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewQueryDTO;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.PastTariffResponseDTO;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.WitsDTO;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
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

    public PastTariffResponseDTO getTariffOverview(TariffOverviewQueryDTO queryDTO) {
        // 

        return null; 
    }

}
