package com.tariff.calculation.tariffCalc.service;

import java.util.List;
import java.util.Optional;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.TariffDeleteDTO;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;
import com.tariff.calculation.tariffCalc.utility.LemmaUtils;

import org.springframework.stereotype.Service;

@Service
public class CrudServiceImpl implements CrudService {
    
    private final TariffRepo tariffRepo;
    private final ItemRepo itemRepo;
    private final CountryRepo countryRepo;
    
    public CrudServiceImpl(TariffRepo tariffRepo, ItemRepo itemRepo, CountryRepo countryRepo) {
        this.tariffRepo = tariffRepo;
        this.itemRepo = itemRepo;
        this.countryRepo = countryRepo;
    }
    
    // deleters
    public void deleteTariff(TariffDeleteDTO tariffDeleteDTO) {
        Country reportingCountry = countryRepo.findByCountryName(tariffDeleteDTO.reportingCountry())
                .orElseThrow(() -> new IllegalArgumentException("Reporting country not found"));

        Country partnerCountry = countryRepo.findByCountryName(tariffDeleteDTO.partnerCountry())
                .orElseThrow(() -> new IllegalArgumentException("Partner country not found"));

        Item item = itemRepo.findByItemName(LemmaUtils.toSingular(tariffDeleteDTO.item().toLowerCase().trim()) + reportingCountry.getCountryNumber())
                .orElseGet(() ->itemRepo.findByItemName(LemmaUtils.toSingular(tariffDeleteDTO.item().trim()).toLowerCase() 
                            + "general")
                .orElseThrow(() -> new IllegalArgumentException("Item not found for item " + tariffDeleteDTO.item())));
        
        List<Tariff> delete = tariffRepo.findByReportingCountryAndPartnerCountryAndItem(reportingCountry, partnerCountry, item);
        
        if (delete.size() == 0) {
            throw new IllegalArgumentException("This combination doesnt exists");
        }
        
        delete.forEach((tariff) -> {
            tariffRepo.delete(tariff);
        });
    }
    
    public void deleteItem(int id) {
        Optional<Item> item = itemRepo.findById(id);
        if (item.isPresent()) {
            itemRepo.delete(item.get());
        } else {
            throw new IllegalArgumentException("Item id is incorrect");
        }
    }
    
    public void deleteCountry(int countryId) {
        Optional<Country> country = countryRepo.findById(countryId);
        if (country.isPresent()) {
            countryRepo.delete(country.get());
        } else {
            throw new IllegalArgumentException("Country id is incorrect");
        }
    }
    // For testing purposes
    // public void deleteAllTariff() {
    //     tariffRepo.deleteAll();
    // }
}