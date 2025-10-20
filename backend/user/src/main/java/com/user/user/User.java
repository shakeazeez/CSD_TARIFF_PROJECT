package com.user.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

import com.user.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String hashedPassword;

    @ElementCollection
    @CollectionTable(name = "user_history", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "history_key")
    @Column(name = "history_value")
    private Map<Integer, Integer> history;

    // Will add all the ontop stuff on here
    @ElementCollection
    @Enumerated
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "user_role")
    private List<Role> role;

    public User(String username, String hashedPassword, Map<Integer, Integer> history,
            List<Role> role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.history = history;
        this.role = role;
    }
}
