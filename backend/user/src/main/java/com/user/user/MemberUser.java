package com.user.user;


import com.user.enums.Role;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;


@DiscriminatorValue("MEMBER")
@Getter
@Setter
@Entity
public class MemberUser extends User {


    public MemberUser(String username, String hashedPassword,
            Role role) {
        super(username, hashedPassword, role);
    }

}
