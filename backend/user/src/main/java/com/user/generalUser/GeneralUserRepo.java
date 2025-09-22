package com.user.generalUser;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralUserRepo extends JpaRepository<GeneralUser, Integer> {
    
}