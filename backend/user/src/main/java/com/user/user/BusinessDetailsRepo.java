package com.user.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessDetailsRepo extends JpaRepository<BusinessDetails, Integer> {
    
    public Optional<BusinessDetails> findByReportingCountryAndItemIgnoreCase (String reportingCountry, String item);
    
}