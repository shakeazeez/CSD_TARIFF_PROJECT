package com.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import com.user.user.User;
import com.user.user.UserRepo;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepo userRepo;
    
    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }
    
    @Transactional
    public List<Integer> addHistory(String username, Integer tariffId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<Integer, Integer> history = user.getHistory();

        if (history == null) {
            history = new HashMap<>();
        }

        history.put(tariffId, history.getOrDefault(tariffId, 0) + 1);

        user.setHistory(history);
        userRepo.save(user);

        // return the most searched top 5 tariffs
        return history.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())) // sort by frequency in descending order
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Integer> retrieveHistory(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<Integer, Integer> history = user.getHistory();

        // return the most searched top 5 tariffs
        return history.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())) // sort by frequency in descending order
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}