package com.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.validation.constraints.NotNull;

@SpringBootApplication
public class UserApplication {
    
    private static final Dotenv dotenv = Dotenv.configure()
                                        .directory("../../")
                                        .filename(".env")
                                        .load();

	public static void main(String[] args) {
        System.setProperty("DATABASE_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("DATABASE_URL", dotenv.get("DATABASE_URL"));
        System.setProperty("SIGNING_SECRET", dotenv.get("SIGNING_SECRET"));
		SpringApplication.run(UserApplication.class, args);
	}
	
	@Bean 
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(@NotNull CorsRegistry registry) {
           	registry.addMapping("/**")
                    .allowedOrigins(dotenv.get("FRONTEND_URL"));
            }
        };
    }

}
