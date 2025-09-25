package com.tariff.calculation.tariffCalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class TariffCalcApplication {    
    private static final Dotenv dotenv = Dotenv.configure()
                                               .directory("./")
                                               .filename(".env")
                                               .load();
	public static void main(String[] args) {
	    System.setProperty("DATABASE_PASSWORD", dotenv.get("DATABASE_PASSWORD"));
					
		System.setProperty("DATABASE_URL", dotenv.get("DATABASE_URL"));
		SpringApplication.run(TariffCalcApplication.class, args);
	}
	

}
