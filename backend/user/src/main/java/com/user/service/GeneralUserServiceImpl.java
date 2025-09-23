package com.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.user.generalUser.GeneralUser;
import com.user.generalUser.GeneralUserRepo;

@Service
public class GeneralUserServiceImpl implements GeneralUserService {

    private final GeneralUserRepo generalUserRepo;

    public GeneralUserServiceImpl(GeneralUserRepo generalUserRepo) {
        this.generalUserRepo = generalUserRepo;
    }

    // public void addHistory(Integer userId, Integer tariffId) {

    // }

    public List<Integer> addPinnedTariff(Integer userId, Integer tariffId) {
        GeneralUser generalUser = generalUserRepo.findById(userId) // does this need to be optional?
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (generalUser.getTariffIds().size() >= 3) {
            throw new IllegalStateException("Cannot pin more than 3 tariffs");
        }

        if (!generalUser.getTariffIds().contains(tariffId)) {
            generalUser.getTariffIds().add(tariffId);
            generalUserRepo.save(generalUser);
        }

        return generalUser.getTariffIds();
    }

    public List<Integer> removePinnedTariff(Integer userId, Integer tariffId) {
        GeneralUser generalUser = generalUserRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (generalUser.getTariffIds().contains(tariffId)) {
            generalUser.getTariffIds().remove(Integer.valueOf(tariffId));
            generalUserRepo.save(generalUser);
        }

        return generalUser.getTariffIds();
    }
}
