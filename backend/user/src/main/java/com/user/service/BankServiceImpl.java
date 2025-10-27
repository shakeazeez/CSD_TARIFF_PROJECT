package com.user.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.user.dto.BankInfoDTO;
import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.BankUser;
import com.user.user.User;
import com.user.user.UserRepo;

import org.springframework.stereotype.Service;

@Service
public class BankServiceImpl implements BankService {

    private UserRepo userRepo;
    private HistoryRepo historyRepo;

    public BankServiceImpl(UserRepo userRepo, HistoryRepo historyRepo) {
        this.userRepo = userRepo;
        this.historyRepo = historyRepo;
    }

    public BankInfoDTO getBankInfo(String username) {

        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });

        if (user instanceof BankUser bUser) {
            List<History> history = historyRepo.findByUser(bUser);
            history.sort((a, b) -> b.getCounter() - a.getCounter());
            
            Map<Integer, LocalDate> store = new HashMap<>();
            
            for (int i = 0; i < Math.min(5, history.size()); i++) {
                History temp = history.get(i);
                store.put(temp.getCounter(), temp.getLocalDate());
            }
            
            return new BankInfoDTO(
                    bUser.getIndustry().toString(),
                    bUser.getOriginCountry(),
                    store
            );
        }

        throw new IllegalAccessError("The user is not a bank user");
    }

}
