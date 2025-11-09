package com.user.service;

import java.util.List;

import com.user.dto.BusinessInfoDTO;
import com.user.dto.BusinessTariffDTO;

public interface BusinessService {

    public BusinessInfoDTO getBusinessDetails(String username);

    public void addTariffRecord(BusinessTariffDTO tariff, String username);

    public void deleteTariffRecord(BusinessTariffDTO tariff, String username);

}
