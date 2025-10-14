package com.user.service;

import java.util.Map;

public interface UserService {
	
    public Map<Integer, Integer> addHistory(String username, Integer tariffId);

    public Map<Integer, Integer> retrieveHistory(String username, Integer tariffId);
}