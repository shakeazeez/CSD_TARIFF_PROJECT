package com.user.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberUserRepo extends JpaRepository<MemberUser, Integer>{

    // Dont know what this specifically needs until properly implemented 
}