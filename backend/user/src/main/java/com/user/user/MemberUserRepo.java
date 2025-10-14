package com.user.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberUserRepo extends JpaRepository<MemberUser, User>{

    // Dont know what this specifically needs until properly implemented 
}