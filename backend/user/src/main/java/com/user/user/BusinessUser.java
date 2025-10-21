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

@Entity
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("business")
@Getter
@Setter
public class BusinessUser extends User {
    
    @ElementCollection
    @CollectionTable(name = "items_sold", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "item_sold")
    @Column(name = "item_sold")
    private List<String> itemsSold;
    
    @ElementCollection
    @CollectionTable(name = "destination_country", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "destination_country")
    @Column(name = "destination_country")
    private List<String> destinationCountries;
    
    @Column(name = "origin_country")
    private String originCountry;

    public BusinessUser(String username, String hashedPassword, Map<Integer, Integer> history,
            List<Role> role,
            List<String> itemsSold,
            List<String> destinationCountries,
            String originCountry) {
        super(username, hashedPassword, history, role);
        
        this.itemsSold = itemsSold;
        this.destinationCountries = destinationCountries;
        this.originCountry = originCountry;
    }

}
