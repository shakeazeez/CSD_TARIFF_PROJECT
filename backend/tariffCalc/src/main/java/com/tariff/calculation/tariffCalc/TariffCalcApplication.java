package com.tariff.calculation.tariffCalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class TariffCalcApplication {
    
    private static String getEnvOrDotenv(String key) {
        String value = System.getenv(key);
        if (value != null) return value;
        try {
            Dotenv dotenv = Dotenv.load();
            return dotenv.get(key);
        } catch (Exception e) {
            return null;
        }
    }
    
	public static void main(String[] args) {
	    System.setProperty("DATABASE_PASSWORD", getEnvOrDotenv("DATABASE_PASSWORD"));
					
		System.setProperty("DATABASE_URL", getEnvOrDotenv("DATABASE_URL"));
		SpringApplication.run(TariffCalcApplication.class, args);
	}
	

}
