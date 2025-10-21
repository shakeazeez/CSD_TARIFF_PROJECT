package com.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.validation.constraints.NotNull;

@SpringBootApplication
public class UserApplication {
    
	public static void main(String[] args) {
        String dbUsername = Utility.getEnvOrDotenv("DB_USERNAME");
        if (dbUsername != null) {
            System.setProperty("DB_USERNAME", dbUsername);
        }
        String dbPassword = Utility.getEnvOrDotenv("DB_PASSWORD");
        if (dbPassword != null) {
            System.setProperty("DB_PASSWORD", dbPassword);
        }
        String openAiKey = Utility.getEnvOrDotenv("OPEN_AI_KEY");
        if (openAiKey != null) {
            System.setProperty("OPEN_AI_KEY", openAiKey);
        }
        String dbUrl = Utility.getEnvOrDotenv("DATABASE_URL");
        if (dbUrl != null) {
            System.setProperty("DATABASE_URL", dbUrl);
        }
        String signingSecret = Utility.getEnvOrDotenv("SIGNING_SECRET");
        if (signingSecret != null) {
            System.setProperty("SIGNING_SECRET", signingSecret);
        }
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
