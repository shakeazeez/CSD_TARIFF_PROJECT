package com.user.service;

import java.util.List;

import com.user.user.User;

public interface UserService {

    public List<Integer> addHistory(String username, Integer tariffId);

    public List<Integer> retrieveHistory(String username);
    
    public List<User> getAllUsers();
}