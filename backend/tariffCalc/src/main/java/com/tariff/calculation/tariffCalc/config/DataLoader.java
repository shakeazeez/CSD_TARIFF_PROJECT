package com.tariff.calculation.tariffCalc.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private CountryRepo countryRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("DataLoader running");
        if (countryRepository.count() == 0) {
            log.info("Loading countries from CSV");
            ClassPathResource resource = new ClassPathResource("data/countries.csv");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                boolean firstLine = true;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    String[] fields = line.split(",");
                    if (fields.length < 4) {
                        log.warn("Invalid line: " + line);
                        continue;
                    }
                    try {
                        int countryNumber = Integer.parseInt(fields[0].trim());
                        String countryCode = fields[1].trim();
                        String countryName = fields[2].trim();
                        boolean isDeveloping = Boolean.parseBoolean(fields[3].trim());
                        Country country = new Country();
                        country.setCountryNumber(countryNumber);
                        country.setCountryCode(countryCode);
                        country.setCountryName(countryName);
                        country.setIsDeveloping(isDeveloping);
                        countryRepository.save(country);
                        count++;
                    } catch (Exception e) {
                        log.error("Error parsing line: " + line, e);
                    }
                }
                log.info("Loaded " + count + " countries");
            }
        } else {
            log.info("Countries already loaded, count: " + countryRepository.count());
        }
    }
}