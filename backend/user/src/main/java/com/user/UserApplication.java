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
    
	public static void main(String[] args) {
        System.setProperty("DATABASE_PASSWORD", Utility.getEnvOrDotenv("DATABASE_PASSWORD"));
        System.setProperty("DATABASE_URL", Utility.getEnvOrDotenv("DATABASE_URL"));
        System.setProperty("SIGNING_SECRET", Utility.getEnvOrDotenv("SIGNING_SECRET"));
		SpringApplication.run(UserApplication.class, args);
	}
	
	@Bean 
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(@NotNull CorsRegistry registry) {
           	String frontendUrl = Utility.getEnvOrDotenv("FRONTEND_URL");
           	if (frontendUrl == null || frontendUrl.isEmpty()) {
           		frontendUrl = "http://localhost:80"; // default for tests
           	}
           	registry.addMapping("/**")
                    .allowedOrigins(frontendUrl);
            }
        };
    }

}
