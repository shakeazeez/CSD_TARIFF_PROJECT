package com.user.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenDTO {
    private String username; 
    private String token; 
    private List<Integer> pin; 
    private String industry; 
    private String originCountry;
    private List<String> destinationCountries;
    private List<String> itemsSold;
    private List<Integer> historicalTariffId;
    
    public TokenDTO(String username, String token, List<Integer> historicalTariffId) {
        this.username = username;
        this.token = token;
        this.historicalTariffId = historicalTariffId;
    }
    
    public TokenDTO(String username, String token, List<Integer> pin, List<Integer> historicalTariffId) {
        this.username = username;
        this.token = token;
        this.pin = pin;
        this.historicalTariffId = historicalTariffId;
    }
    
    public TokenDTO(String username, String token, String industry, String originCountry, List<Integer> historicalTariffId) {
        this.username = username;
        this.token = token;
        this.industry = industry;
        this.originCountry = originCountry;
        this.historicalTariffId = historicalTariffId;
    }
    
    public TokenDTO(String username, String token, List<String> itemsSold, List<String> destinationCountries ,String originCountry, List<Integer> historicalTariffId) {
        this.username = username;
        this.token = token;
        this.itemsSold = itemsSold;
        this.destinationCountries = destinationCountries;
        this.originCountry = originCountry;
        this.historicalTariffId = historicalTariffId;
    }
}