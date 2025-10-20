package com.user.service;

import java.util.List;

public interface MemberUserService {

    public List<Integer> addPinnedTariff(String username, Integer tariffId);

    public List<Integer> removePinnedTariff(String username, Integer tariffId);

}