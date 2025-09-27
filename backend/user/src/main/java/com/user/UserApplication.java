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
        System.setProperty("SIGNING_SECRET", getEnvOrDotenv("SIGNING_SECRET"));
		SpringApplication.run(UserApplication.class, args);
	}
	
	@Bean 
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(@NotNull CorsRegistry registry) {
           	registry.addMapping("/**")
                    .allowedOrigins(getEnvOrDotenv("FRONTEND_URL"));
            }
        };
    }

}
