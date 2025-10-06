package com.user.security.service;

import java.util.Date;
import java.util.List;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.user.security.enums.Role;
import com.user.security.user.AuthUser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final String ROLES_CLAIM = "roles";
    
    private final Algorithm signingAlgo;
    
    public JwtService(@Value("${jwt.signing-secret}") String signingSecret) {
        // System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + signingSecret);
        if (signingSecret == null) {
            throw new IllegalArgumentException("WHoops"); 
        }
        this.signingAlgo = Algorithm.HMAC512(signingSecret);
    }
    
    public AuthUser resolveJwtToken(String token) throws JWTVerificationException {
        
        JWTVerifier jwtVerifier = JWT.require(signingAlgo).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(token);
        
        String userId = decodedJWT.getSubject();
        List<Role> roles = decodedJWT.getClaim(ROLES_CLAIM).asList(Role.class);
        return new AuthUser(userId, "", roles);
    }
    
    public String createJwtToken(AuthUser authUser) {
        /*
         * A token requires a start and end creation inorder to ensure
         * it doesnt break
         */
        long nowMilles = System.currentTimeMillis();
        Date now = new Date(nowMilles);
        long expMilles = System.currentTimeMillis() + 86400;
        Date exp = new Date(expMilles);

        List<String> roles = authUser.getRoles()
                                     .stream()
                                     .map(Role::name)
                                     .toList();

        return JWT.create()
                  .withSubject(authUser.getUserId())
                  .withClaim(ROLES_CLAIM, roles)
                  .withIssuedAt(now)
                  .withExpiresAt(exp)
                  .sign(signingAlgo);
    }
}