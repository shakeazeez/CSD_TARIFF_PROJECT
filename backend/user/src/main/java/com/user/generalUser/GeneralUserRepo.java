package com.user.generalUser;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralUserRepo extends JpaRepository<GeneralUser, Integer> {

    Optional<GeneralUser> findById(Integer id);
    Optional<GeneralUser> findByUsername(String username);

}