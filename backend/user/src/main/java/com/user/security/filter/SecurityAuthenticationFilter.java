package com.user.security.filter;

import java.io.IOException;

import com.user.security.enums.AuthConstants;
import com.user.security.exception.TokenAuthenticationException;
import com.user.security.service.JwtService;
import com.user.security.user.AuthUser;
import com.user.security.user.UserAuthentication;

import org.springframework.boot.autoconfigure.webservices.WebServicesProperties.Servlet;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SecurityAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    
    public SecurityAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
    
        String authenticationHeader = request.getHeader("Authorization");

        if (authenticationHeader == null) {
          authenticationHeader = request.getHeader("AuthConstants.AUTHORIZATION_HEADER.toString()");
        }

        // String authenticationHeader = request.getHeader(AuthConstants.AUTHORIZATION_HEADER.toString());
    
        if (authenticationHeader == null) {
          // Authentication token is not present, let's rely on anonymous authentication
          filterChain.doFilter(request, response);
          return;
        }
    
        String jwtToken = stripBearerPrefix(authenticationHeader);
        AuthUser authUser = jwtService.resolveJwtToken(jwtToken);
    
        UserAuthentication authentication = new UserAuthentication(authUser);
    
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    
        filterChain.doFilter(request, response);
    }
    
    private String stripBearerPrefix(String token) {
    
        if (!token.startsWith("Bearer")) {
          throw new TokenAuthenticationException("Unsupported authentication scheme");
        }
    
        return token.substring(7);
    }
}