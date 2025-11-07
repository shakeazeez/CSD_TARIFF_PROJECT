package com.user.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.user.user.User;
import com.user.dto.UserInfoDTO;

public interface UserService {

    public Map<Integer, LocalDate> addHistory(String username, Integer tariffId);

    public Map<Integer, LocalDate> retrieveHistory(String username, int limiter);
    
    public List<User> getAllUsers();
    
    
    public List<Integer> addPinnedTariff(String username, Integer tariffId);

    public List<Integer> removePinnedTariff(String username, Integer tariffId);
    
    public UserInfoDTO getPinnedTariff(String username);
}