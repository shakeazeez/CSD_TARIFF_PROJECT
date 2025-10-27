package com.user.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.user.dto.BusinessInfoDTO;
import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.BusinessUser;
import com.user.user.User;
import com.user.user.UserRepo;

import org.springframework.stereotype.Service;

@Service
public class BusinessServiceImpl implements BusinessService {

    private UserRepo userRepo;
    private HistoryRepo historyRepo;

    public BusinessServiceImpl(UserRepo userRepo, HistoryRepo historyRepo) {
        this.userRepo = userRepo;
        this.historyRepo = historyRepo;
    }

    public BusinessInfoDTO getBusinessDetails(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });

        if (user instanceof BusinessUser bUser) {
            List<History> history = historyRepo.findByUser(bUser);
            history.sort((a, b) -> b.getCounter() - a.getCounter());
            
            Map<Integer, LocalDate> store = new HashMap<>();
            
            for (int i = 0; i < Math.min(5, history.size()); i++) {
                History temp = history.get(i);
                store.put(temp.getCounter(), temp.getLocalDate());
            }
            
            return new BusinessInfoDTO(
                bUser.getOriginCountry(),
                bUser.getItemsSold(),
                bUser.getDestinationCountries(),
                store
            );
            
        }

        throw new IllegalAccessError("The user is not a business user");

    }

}
