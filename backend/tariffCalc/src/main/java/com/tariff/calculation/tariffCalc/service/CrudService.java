package com.tariff.calculation.tariffCalc.service;

import com.tariff.calculation.tariffCalc.dto.TariffDeleteDTO;

public interface CrudService {
    public void deleteTariff(TariffDeleteDTO tariffDeleteDTO); 
    public void deleteItem(int id); 
    public void deleteCountry(int countryId); 
    // For testing purposes
    // public void deleteAllTariff(); 
}