package com.user.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.user.dto.BankInfoDTO;
import com.user.user.BankUser;
import com.user.user.User;
import com.user.user.UserRepo;

import org.springframework.stereotype.Service;

@Service
public class BankServiceImpl implements BankService {

    private UserRepo userRepo;

    public BankServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public BankInfoDTO getBankInfo(String username) {

        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });

        if (user instanceof BankUser bUser) {
            List<Integer> historyId = bUser.getHistory().entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
            return new BankInfoDTO(
                    bUser.getIndustry().toString(),
                    bUser.getOriginCountry(),
                    historyId
            );
        }

        throw new IllegalAccessError("The user is not a bank user");
    }

}
