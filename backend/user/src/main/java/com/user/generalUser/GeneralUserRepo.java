package com.user.generalUser;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GeneralUserRepo extends JpaRepository<GeneralUser, Integer> {
    // Getters
    public Optional<GeneralUser> findByUsername(String userName);
}