package com.user.user;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class BusinessDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tariff_id")
    private Integer id;
    
    @Column(name = "reporting_country")
    private String reportingCountry;
    @Column(name = "item_name")
    private String item;    
    
    @ManyToMany(mappedBy = "tariffData")
    private List<BusinessUser> businessUsers;
	
    
    public BusinessDetails(String reportingCountry, String item) {
        this.reportingCountry = reportingCountry;
        this.item = item;
    }
}