package com.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.user.generalUser.GeneralUser;
import com.user.generalUser.GeneralUserRepo;

@Service
public class GeneralUserServiceImpl implements GeneralUserService {

    private final GeneralUserRepo generalUserRepo;

    public GeneralUserServiceImpl(GeneralUserRepo generalUserRepo) {
        this.generalUserRepo = generalUserRepo;
    }

    @Transactional
    public Map<Integer, Integer> addHistory(String username, Integer tariffId) {
        GeneralUser generalUser = generalUserRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<Integer, Integer> history = generalUser.getHistory();

        if (history == null) {
            history = new HashMap<>();
        }

        history.put(tariffId, history.getOrDefault(tariffId, 0) + 1);

        generalUser.setHistory(history);
        generalUserRepo.save(generalUser);

        return history;
    }

    @Transactional
    public Map<Integer, Integer> retrieveHistory(String username, Integer tariffId) {
        GeneralUser generalUser = generalUserRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return generalUser.getHistory();
    }

    @Transactional
    public List<Integer> addPinnedTariff(Integer userId, Integer tariffId) {
        GeneralUser generalUser = generalUserRepo.findById(userId)
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

    @Transactional
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
