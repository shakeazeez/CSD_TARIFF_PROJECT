package com.tariff.calculation.tariffCalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.tariff.calculation.tariffCalc.utility.LemmaUtils;

@SpringBootApplication
public class TariffCalcApplication {
    
	public static void main(String[] args) {
	    System.setProperty("DATABASE_PASSWORD", LemmaUtils.getEnvOrDotenv("DATABASE_PASSWORD"));
					
		System.setProperty("DATABASE_URL", LemmaUtils.getEnvOrDotenv("DATABASE_URL"));
		SpringApplication.run(TariffCalcApplication.class, args);
	}
	

}
