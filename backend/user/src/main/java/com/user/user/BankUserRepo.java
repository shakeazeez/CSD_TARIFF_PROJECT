package com.user.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankUserRepo extends JpaRepository<BankUser, Integer> {
	
} 