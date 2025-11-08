package com.user.dto;

import java.util.List;

public record CreateUserDTO (
    String username, 
    String password, 
    String role, 
    String industry,
    String originCountry,
    List<BusinessTariffDTO> tariffs
) {}