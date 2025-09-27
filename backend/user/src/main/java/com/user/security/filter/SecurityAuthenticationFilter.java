package com.user.security.filter;

import java.io.IOException;

import com.user.security.enums.AuthConstants;
import com.user.security.exception.TokenAuthenticationException;
import com.user.security.service.JwtService;
import com.user.security.user.AuthUser;
import com.user.security.user.UserAuthentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final Logger log = LoggerFactory.getLogger(SecurityAuthenticationFilter.class);
    
    public SecurityAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
            
        String authenticationHeader = request.getHeader("Authorization");
        log.info("Authorization header: " + authenticationHeader);
    
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
        // UsernamePasswordAuthenticationToken authentication =
        //     new UsernamePasswordAuthenticationToken(
        //         authUser, // principal
        //         null,     // credentials
        //         authUser.getRoles().stream()
        //             .map(Enum::name)
        //             .map(SimpleGrantedAuthority::new)
        //             .toList()
        //     );
        
        // SecurityContext context = SecurityContextHolder.createEmptyContext();
        // context.setAuthentication(authentication);
        // SecurityContextHolder.setContext(context);
        filterChain.doFilter(request, response);
    }
    
    private String stripBearerPrefix(String token) {
        // log.info("Printing token: " + token + "\n\n\n\n\n\n");
        if (!token.startsWith("Bearer")) {
          throw new TokenAuthenticationException("Unsupported authentication scheme");
        }
    
        return token.substring(7);
    }
}