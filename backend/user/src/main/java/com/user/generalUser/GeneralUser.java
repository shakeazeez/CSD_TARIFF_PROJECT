package com.user.generalUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

import com.user.security.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "general_user")
public class GeneralUser {
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

    @ElementCollection
    @CollectionTable(name = "user_tariff_ids", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "tariff_id")
    private List<Integer> tariffIds;

    // Will add all the ontop stuff on here
    @ElementCollection
    @Enumerated
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "user_role")
    private List<Role> role;

    public GeneralUser(String username, String hashedPassword, Map<Integer, Integer> history, List<Integer> tariffIds,
            List<Role> role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.history = history;
        this.tariffIds = tariffIds;
        this.role = role;
    }
}
