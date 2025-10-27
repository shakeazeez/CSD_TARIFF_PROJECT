package com.user.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.user.user.User;

public interface UserService {

    public Map<Integer, LocalDate> addHistory(String username, Integer tariffId);

    public Map<Integer, LocalDate> retrieveHistory(String username);
    
    public List<User> getAllUsers();
}