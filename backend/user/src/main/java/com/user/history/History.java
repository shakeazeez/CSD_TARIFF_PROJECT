package com.user.history;

import java.time.LocalDate;

import com.user.user.User;

import org.springframework.data.annotation.Id;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
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
    @Column(name = "user")
    private User user;
    
    
    public History(int tariffId, User user) {
        this.tariffId = tariffId;
        this.counter = 1;
        this.localDate = LocalDate.now();
        this.user = user;
    }
}

