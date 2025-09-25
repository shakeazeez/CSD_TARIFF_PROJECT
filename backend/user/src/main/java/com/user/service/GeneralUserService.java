package com.user.service;

import java.util.List;
import java.util.Map;

public interface GeneralUserService {

    public Map<Integer, Integer> addHistory(String username, Integer tariffId);

    public Map<Integer, Integer> retrieveHistory(String username, Integer tariffId);

    public List<Integer> addPinnedTariff(Integer userId, Integer tariffId);

    public List<Integer> removePinnedTariff(Integer userId, Integer tariffId);

}