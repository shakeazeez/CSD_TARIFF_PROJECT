package com.user.service;

import java.util.List;

import com.user.dto.BusinessInfoDTO;

public interface BusinessService {

    public BusinessInfoDTO getBusinessDetails(String username);

    public void deleteDestinationCountry(List<String> countries, String username);

    public void addDestinationCountry(List<String> countries, String username);

    public void deleteItemsSold(List<String> items, String username);

    public void addItemsSold(List<String> items, String username);
}
