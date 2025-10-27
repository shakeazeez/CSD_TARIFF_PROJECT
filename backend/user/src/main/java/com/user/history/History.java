package com.user.history;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.user.user.User;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class History {

    @Id
    @Column(name = "hist_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "tariff_id")
    private int tariffId;
    
    @Column(name = "num_of_occurences")
    private int counter;
    
    @Column(name = "latest_date")
    private LocalDate localDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
    
    
    public History(int tariffId, User user) {
        this.tariffId = tariffId;
        this.counter = 1;
        this.localDate = LocalDate.now();
        this.user = user;
    }
}

