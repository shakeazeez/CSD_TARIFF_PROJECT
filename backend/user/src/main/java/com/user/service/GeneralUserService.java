package com.user.service;

import java.util.List;

public interface GeneralUserService {

    public List<Integer> addPinnedTariff(Integer userId, Integer tariffId);

    public List<Integer> removePinnedTariff(Integer userId, Integer tariffId);

    // public List<Integer> getTariffHistory(Integer userId);

}