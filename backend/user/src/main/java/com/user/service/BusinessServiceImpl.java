package com.user.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.user.dto.BusinessInfoDTO;
import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.BusinessUser;
import com.user.user.User;
import com.user.user.UserRepo;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BusinessServiceImpl implements BusinessService {

    private UserRepo userRepo;
    private HistoryRepo historyRepo;
    private final Logger log = LoggerFactory.getLogger(BusinessServiceImpl.class);

    public BusinessServiceImpl(UserRepo userRepo, HistoryRepo historyRepo) {
        this.userRepo = userRepo;
        this.historyRepo = historyRepo;
    }
    
    @Transactional
    public void addItemsSold(List<String> items, String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });

        log.info("Class of user " + user.getClass());
        if (user instanceof BusinessUser bUser) {
            Set<String> itemName = bUser.getItemsSold();
            items.stream().map(a -> a.toLowerCase()).forEach(a -> itemName.add(a));
            log.info("No problem adding");
            bUser.setItemsSold(itemName);
            userRepo.save(bUser);
            return;
        }

        throw new IllegalAccessError("The user is not a business user");
    }
    
    @Transactional
    public void deleteItemsSold(List<String> items, String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });

        if (user instanceof BusinessUser bUser) {
            Set<String> itemName = bUser.getItemsSold();
            items.forEach(item -> itemName.remove(item));
            bUser.setItemsSold(itemName);
            userRepo.save(bUser);
            return;
        }

        throw new IllegalAccessError("The user is not a business user");
    }
    
    @Transactional
    public void addDestinationCountry(List<String> countries, String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });
        log.info("Class of user " + user.getClass());
        log.info(user.getRole().toString());
        
        if (user instanceof BusinessUser bUser) {
            Set<String> countryName = bUser.getDestinationCountries();
            countries.stream().map(a -> a.toLowerCase()).forEach(a -> countryName.add(a));
            bUser.setDestinationCountries(countryName);
            userRepo.save(bUser);
            return;
        }

        throw new IllegalAccessError("The user is not a business user");
    }
    
    @Transactional
    public void deleteDestinationCountry(List<String> countries, String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> {
            return new IllegalArgumentException("Unable to retrieve this account");
        });

        if (user instanceof BusinessUser bUser) {
            Set<String> countryName = bUser.getDestinationCountries();
            countries.forEach(country -> countryName.remove(country));
            bUser.setDestinationCountries(countryName);
            userRepo.save(bUser);
            return;
        }

        throw new IllegalAccessError("The user is not a business user");
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
                // query origin is reporting 
                bUser.getOriginCountry(),
                // item queried list
                new ArrayList<>(bUser.getItemsSold()),
                // partner country list 
                new ArrayList<>(bUser.getDestinationCountries()),
                store
            );
            
        }

        throw new IllegalAccessError("The user is not a business user");

    }

}
