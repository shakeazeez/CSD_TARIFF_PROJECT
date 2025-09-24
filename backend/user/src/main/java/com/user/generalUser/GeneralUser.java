package com.user.generalUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.user.security.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;

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
    @Column(name = "history_value")
    private List<Integer> history;
    
    @ElementCollection
    @CollectionTable(name = "user_tariff_ids", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "tariff_id")
    private List<Integer> tariffIds;
    
    // Will add all the ontop stuff on here
    private List<Role> role;

    public GeneralUser(String username, String hashedPassword, List<Integer> history, List<Integer> tariffIds) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.history = history;
        this.tariffIds = tariffIds;
    }
}
