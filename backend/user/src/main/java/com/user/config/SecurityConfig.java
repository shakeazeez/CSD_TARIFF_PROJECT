package com.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .headers(headers -> headers.httpStrictTransportSecurity(
                    hsts -> hsts.disable()
                )
            )
            // The below is not ready for actual use yet
            // ).addFilterBefore()
            .authorizeHttpRequests (
                matcher -> matcher.requestMatchers(
                    "/user/**"
                ).authenticated()
                .requestMatchers(
                    "/tariff/**"
                ).permitAll()
            ).headers(header -> header.cacheControl(
                cache -> cache.disable()
            )); 
            // To add extra once general security is settled 
        
        
        return http.build();
    }
    
    
    @Bean
    public AuthenticationManager noOpAuthenticationManager() {
      return authentication -> null;
    }
} 