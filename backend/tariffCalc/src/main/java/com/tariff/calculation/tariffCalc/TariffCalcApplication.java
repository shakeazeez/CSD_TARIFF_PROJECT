package com.tariff.calculation.tariffCalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.validation.constraints.NotNull;

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
