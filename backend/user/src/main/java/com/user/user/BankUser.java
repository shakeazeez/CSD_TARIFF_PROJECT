package com.user.user;

import java.util.List;
import java.util.Map;

import com.user.enums.Industry;
import com.user.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@DiscriminatorValue("BANK")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BankUser extends User {
    @Enumerated
    @Column(name = "industry")
    private Industry industry;

    @Column(name = "origin_country")
    private String originCountry;

    public BankUser(String username, String hashedPassword,
            Role role,
            Industry industry,
            String originCountry) {

        super(username, hashedPassword, role);
        this.industry = industry;
        this.originCountry = originCountry;
    }

}
