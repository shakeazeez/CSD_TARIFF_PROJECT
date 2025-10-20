package com.user.service;

import java.util.HashMap;
import java.util.Map;

import com.user.user.User;
import com.user.user.UserRepo;

import org.springframework.transaction.annotation.Transactional;

public class UserServiceImpl implements UserService {
    
    private final UserRepo userRepo;
    
    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }
	
    
    @Transactional
    public Map<Integer, Integer> addHistory(String username, Integer tariffId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<Integer, Integer> history = user.getHistory();

        if (history == null) {
            history = new HashMap<>();
        }

        history.put(tariffId, history.getOrDefault(tariffId, 0) + 1);

        user.setHistory(history);
        userRepo.save(user);

        return history;
    }

    @Transactional
    public Map<Integer, Integer> retrieveHistory(String username, Integer tariffId) {
        User generalUser = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return generalUser.getHistory();
    }
}