package com.user.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.user.dto.BusinessInfoDTO;
import com.user.user.BusinessUser;
import com.user.user.User;
import com.user.user.UserRepo;

import org.springframework.stereotype.Service;

@Service
public class BusinessServiceImpl extends BusinessUser {

    private UserRepo userRepo;

    public BusinessServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public BusinessInfoDTO getBusinessDetails(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });

        if (user instanceof BusinessUser bUser) {
            List<Integer> historyId = bUser.getHistory().entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
            return new BusinessInfoDTO (
                bUser.getOriginCountry(),
                bUser.getItemsSold(),
                bUser.getDestinationCountries(),
                historyId
            );
        }

        throw new IllegalAccessError("The user is not a business user");

    }

}
