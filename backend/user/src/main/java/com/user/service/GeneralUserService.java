package com.user.service;

import java.util.List;
import java.util.Map;

public interface GeneralUserService {

    public Map<Integer, Integer> addHistory(String username, Integer tariffId);

    public Map<Integer, Integer> retrieveHistory(String username, Integer tariffId);

    public List<Integer> addPinnedTariff(String username, Integer tariffId);

    public List<Integer> removePinnedTariff(String username, Integer tariffId);

}