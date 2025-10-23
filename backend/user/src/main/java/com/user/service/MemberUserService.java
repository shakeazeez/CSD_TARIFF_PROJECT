package com.user.service;

import java.util.List;

import com.user.dto.MemberInfoDTO;

public interface MemberUserService {

    public List<Integer> addPinnedTariff(String username, Integer tariffId);

    public List<Integer> removePinnedTariff(String username, Integer tariffId);
    
    public MemberInfoDTO getPinnedTariff(String username);

}