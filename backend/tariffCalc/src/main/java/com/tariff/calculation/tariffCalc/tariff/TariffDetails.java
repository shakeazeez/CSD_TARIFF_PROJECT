package com.tariff.calculation.tariffCalc.tariff;

import java.util.*;
import com.tariff.calculation.tariffCalc.country.Country;

public class TariffDetails {
    private Country country;
    private List<Tariff> topTenTariffList;
    private double currentRate;
    private double averageRate;
}
