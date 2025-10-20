package com.user.service;

import java.util.List;

public interface UserService {

    public List<Integer> addHistory(String username, Integer tariffId);

    public List<Integer> retrieveHistory(String username);
}