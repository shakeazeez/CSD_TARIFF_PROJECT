package com.user.user;

import java.util.List;
import java.util.Map;

import com.user.enums.Role;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("member")
@Getter
@Setter
@Entity
public class MemberUser extends User {
    @ElementCollection
    @CollectionTable(name = "pinned_tariff", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "pinned_tariff")
    @Column(name = "pinned_tariffs")
    private List<Integer> pinnedTariffId;

    public MemberUser(String username, String hashedPassword,
            List<Integer> pinnedTariffId,
            List<Role> role) {
        super(username, hashedPassword, role);
        this.pinnedTariffId = pinnedTariffId;
    }

}
