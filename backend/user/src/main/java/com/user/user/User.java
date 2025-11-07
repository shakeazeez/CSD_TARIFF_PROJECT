package com.user.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.user.enums.Role;
import com.user.history.History;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "\"user\"")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "hashedpassword")
    private String hashedPassword;
    // @ElementCollection
    // @CollectionTable(name = "user_history", joinColumns = @JoinColumn(name =
    // "user_id"))
    // @MapKeyColumn(name = "history_key")
    // @Column(name = "history_value")
    // private Map<Integer, Integer> history;

    @OneToMany(mappedBy = "user")
    private List<History> history;

    // Will add all the ontop stuff on here
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "user_roles")
    private Role role;
    
    @ElementCollection
    @CollectionTable(name = "pinned_tariff", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "pinned_tariff")
    @Column(name = "pinned_tariffs")
    private List<Integer> pinnedTariffId;

    public User(String username, String hashedPassword,
            Role role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.history = new ArrayList<>();
        this.role = role;
        this.pinnedTariffId = new ArrayList<>();
    }
}
