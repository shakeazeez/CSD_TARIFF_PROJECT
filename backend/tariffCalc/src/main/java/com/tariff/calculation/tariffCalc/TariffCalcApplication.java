package com.tariff.calculation.tariffCalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.tariff.calculation.tariffCalc.utility.LemmaUtils;

@SpringBootApplication
public class TariffCalcApplication {
    
	public static void main(String[] args) {
	    String dbUsername = LemmaUtils.getEnvOrDotenv("DB_USERNAME");
	    if (dbUsername != null) {
	        System.setProperty("DB_USERNAME", dbUsername);
	    }
	    String dbPassword = LemmaUtils.getEnvOrDotenv("DB_PASSWORD");
	    if (dbPassword != null) {
	        System.setProperty("DB_PASSWORD", dbPassword);
	    }
	    String openAiKey = LemmaUtils.getEnvOrDotenv("OPEN_AI_KEY");
	    if (openAiKey != null) {
	        System.setProperty("OPEN_AI_KEY", openAiKey);
	    }
	    String dbUrl = LemmaUtils.getEnvOrDotenv("DATABASE_URL");
	    if (dbUrl != null) {
	        System.setProperty("DATABASE_URL", dbUrl);
	    }
		SpringApplication.run(TariffCalcApplication.class, args);
	}
	

}
