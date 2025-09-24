package com.user.security.user;

import java.util.Collection;
import java.util.List;

import com.user.security.enums.Role;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser implements UserDetails {
    
    private String userId;
    private String passHash;
    private List<Role> roles;
    
    @Override
     public Collection<? extends GrantedAuthority> getAuthorities() {
       return roles.stream()
                   .map(role -> new SimpleGrantedAuthority(role.name()))
                   .toList();
     }
    
     @Override
     public String getPassword() {
       return passHash;
     }
    
    
     @Override
     public String getUsername() {
       return null;
     }
    
    
     @Override
     public boolean isAccountNonExpired() {
       return true;
     }
    
    
     @Override
     public boolean isAccountNonLocked() {
       return true;
     }
    
    
     @Override
     public boolean isCredentialsNonExpired() {
       return true;
     }
    
    
     @Override
     public boolean isEnabled() {
       return true;
     }
    
    
}