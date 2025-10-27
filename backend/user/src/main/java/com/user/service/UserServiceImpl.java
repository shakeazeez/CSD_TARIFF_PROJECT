package com.user.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.User;
import com.user.user.UserRepo;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final HistoryRepo historyRepo;

    public UserServiceImpl(UserRepo userRepo, HistoryRepo historyRepo) {
        this.userRepo = userRepo;
        this.historyRepo = historyRepo;
    }

    @Transactional
    public Map<Integer, LocalDate> addHistory(String username, Integer tariffId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<History> historyEntry = historyRepo.findByTariffIdAndUser(tariffId, user);

        if (historyEntry.isEmpty()) {
            historyRepo.save(new History(tariffId, user));
        } else {
            History temp = historyEntry.get();
            temp.setCounter(temp.getCounter() + 1);
            temp.setLocalDate(LocalDate.now());
            historyRepo.save(temp);
        }

        // return the most searched top 5 tariffs
        List<History> searched = historyRepo.findByUser(user);
        searched.sort((a, b) -> b.getCounter() - a.getCounter());

        Map<Integer, LocalDate> res = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(5, searched.size()); i++) {
            History temp = searched.get(i);
            res.put(temp.getCounter(), temp.getLocalDate());
        }

        return res;
    }

    @Transactional
    public Map<Integer, LocalDate> retrieveHistory(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<History> searched = historyRepo.findByUser(user);
        searched.sort((a, b) -> b.getCounter() - a.getCounter());

        Map<Integer, LocalDate> res = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(5, searched.size()); i++) {
            History temp = searched.get(i);
            res.put(temp.getCounter(), temp.getLocalDate());
        }

        // return the most searched top 5 tariffs
        return res;
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}
