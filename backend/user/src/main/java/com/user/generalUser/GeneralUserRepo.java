package com.user.generalUser;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GeneralUserRepo extends JpaRepository<GeneralUser, Integer> {

    Optional<GeneralUser> findById(Integer id);
    Optional<GeneralUser> findByUsername(String username);


}