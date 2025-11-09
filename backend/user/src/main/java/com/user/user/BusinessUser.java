package com.user.user;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.user.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("BUSINESS")
@Getter
@Setter
public class BusinessUser extends User {

    // @ElementCollection(fetch = FetchType.EAGER)
    // @CollectionTable(name = "items_sold", joinColumns = @JoinColumn(name = "user_id"))
    // @MapKeyColumn(name = "item_sold")
    // @Column(name = "item_sold")
    // private Set<String> itemsSold;

    // @ElementCollection
    // @CollectionTable(name = "destination_country", joinColumns = @JoinColumn(name = "user_id"))
    // @MapKeyColumn(name = "destination_country")
    // @Column(name = "destination_country")
    // private Set<String> destinationCountries;
    
    
    @ManyToMany
    @JoinTable(name = "tariff_data_relationship",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "tariff_id")
    )
    private Set<BusinessDetails> tariffData;

    @Column(name = "origin_country")
    private String originCountry;

    public BusinessUser(String username, String hashedPassword,
            Role role,
            Set<BusinessDetails> tariffData,
            String originCountry) {
        super(username, hashedPassword, role);
        this.tariffData = new HashSet<>();
        // System.out.println(itemsSold.toString());
        this.originCountry = originCountry;
    }

}
