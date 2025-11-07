package com.tariff.calculation.tariffCalc.tariff;

import java.util.*;
import com.tariff.calculation.tariffCalc.country.Country;

public class TariffDetails {
    private Country country;
    private List<Tariff> tariffList;
    private double averageRate;

    public TariffDetails(Country country, List<Tariff> tariffList, double averageRate) {
        this.country = country;
        this.tariffList = tariffList;
        this.averageRate = averageRate;
    }

    public Country getCountry() {
        return country;
    }

    public List<Tariff> getTariffList() {
        return tariffList;
    }

    public double getAverageRate() {
        return averageRate;
    }
}
