package com.user.service;

import java.util.List;
import java.util.Map;

public interface GeneralUserService {

    public List<Integer> addHistory(String username, Integer tariffId);

    public List<Integer> retrieveHistory(String username);

    public List<Integer> addPinnedTariff(String username, Integer tariffId);

    public List<Integer> removePinnedTariff(String username, Integer tariffId);

}