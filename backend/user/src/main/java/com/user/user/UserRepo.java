package com.user.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findById(Integer id);
    Optional<User> findByUsername(String username);
}