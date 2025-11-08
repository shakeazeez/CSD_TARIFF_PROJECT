package com.user.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.user.dto.BusinessInfoDTO;
import com.user.dto.BusinessTariffDTO;
import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.BusinessDetails;
import com.user.user.BusinessDetailsRepo;
import com.user.user.BusinessUser;
import com.user.user.User;
import com.user.user.UserRepo;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BusinessServiceImpl implements BusinessService {

    private UserRepo userRepo;
    private HistoryRepo historyRepo;
    private BusinessDetailsRepo businessDetailsRepo;
    private final Logger log = LoggerFactory.getLogger(BusinessServiceImpl.class);

    public BusinessServiceImpl(UserRepo userRepo, HistoryRepo historyRepo, BusinessDetailsRepo businessDetailsRepo) {
        this.userRepo = userRepo;
        this.historyRepo = historyRepo;
        this.businessDetailsRepo = businessDetailsRepo;
    }

    @Transactional
    public void addTariffRecord(BusinessTariffDTO tariff, String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });

        log.info("Class of user " + user.getClass());
        if (user instanceof BusinessUser bUser) {
            BusinessDetails tariffRecord = businessDetailsRepo
                    .findByReportingCountryAndItemIgnoreCase(tariff.reportingCountry(), tariff.item())
                    .orElseGet(() -> businessDetailsRepo.save(new BusinessDetails(tariff.reportingCountry(), tariff.item())));
            
            bUser.getTariffData().add(tariffRecord);
            log.info("No problem adding");
            userRepo.save(bUser);
            return;
        }

        throw new IllegalAccessError("The user is not a business user");
    }
    
    @Transactional
    public void deleteTariffRecord(BusinessTariffDTO tariff, String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });

        if (user instanceof BusinessUser bUser) {
            BusinessDetails tariffRecord = businessDetailsRepo
                    .findByReportingCountryAndItemIgnoreCase(tariff.reportingCountry(), tariff.item())
                    .orElseGet(() -> businessDetailsRepo.save(new BusinessDetails(tariff.reportingCountry(), tariff.item())));
            bUser.getTariffData().remove(tariffRecord);
            userRepo.save(bUser);
            return;
        }

        throw new IllegalAccessError("The user is not a business user");
    }

    public BusinessInfoDTO getBusinessDetails(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });

        if (user instanceof BusinessUser bUser) {
            List<History> history = historyRepo.findByUser(bUser);
            history.sort((a, b) -> b.getCounter() - a.getCounter());

            Map<Integer, LocalDate> store = new HashMap<>();

            for (int i = 0; i < Math.min(5, history.size()); i++) {
                History temp = history.get(i);
                store.put(temp.getCounter(), temp.getLocalDate());
            }

            List<BusinessTariffDTO> tariff = bUser.getTariffData().stream()
                    .map(a -> new BusinessTariffDTO(a.getReportingCountry(), a.getItem()))
                    .collect(Collectors.toCollection(ArrayList::new));

            return new BusinessInfoDTO(
                    // query origin is reporting
                    bUser.getOriginCountry(),
                    tariff,
                    store);

        }

        throw new IllegalAccessError("The user is not a business user");

    }

}
