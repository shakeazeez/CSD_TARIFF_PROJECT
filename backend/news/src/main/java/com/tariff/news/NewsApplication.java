package com.tariff.news;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class NewsApplication {

	public static void main(String[] args) {
		// Load environment variables from .env file
		loadEnvironmentVariables();
		SpringApplication.run(NewsApplication.class, args);
	}

	private static void loadEnvironmentVariables() {
		try {
			Dotenv dotenv = Dotenv.load();
			String openAiKey = dotenv.get("OPEN_AI_KEY");
			if (openAiKey != null) {
				System.setProperty("OPEN_AI_KEY", openAiKey);
			}
			String thenewsApiKey = dotenv.get("THE_NEWS_API_KEY");
			if (thenewsApiKey != null) {
				System.setProperty("THE_NEWS_API_KEY", thenewsApiKey);
			}
			String dbUrl = dotenv.get("DATABASE_URL");
			if (dbUrl != null) {
				System.setProperty("DATABASE_URL", dbUrl);
			}
			String dbUsername = dotenv.get("DB_USERNAME");
			if (dbUsername != null) {
				System.setProperty("DB_USERNAME", dbUsername);
			}
			String dbPassword = dotenv.get("DB_PASSWORD");
			if (dbPassword != null) {
				System.setProperty("DB_PASSWORD", dbPassword);
			}
		} catch (Exception e) {
			System.out.println("Warning: Could not load .env file: " + e.getMessage());
		}
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
					.allowedOriginPatterns("*")
					.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
					.allowedHeaders("*")
					.allowCredentials(true);
			}
		};
	}
}