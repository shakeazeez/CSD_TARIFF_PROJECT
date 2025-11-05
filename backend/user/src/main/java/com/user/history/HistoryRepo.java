package com.user.history;

import java.util.List;
import java.util.Optional;

import com.user.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepo extends JpaRepository<History, Integer>{
    
    public Optional<History> findByTariffIdAndUser(int tariffId, User user);
    
    public List<History> findByUser(User user);
	 
}