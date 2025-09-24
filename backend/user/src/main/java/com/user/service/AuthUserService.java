package com.user.service;

import com.user.dto.TokenDTO;

public interface AuthUserService{
     public TokenDTO login(String username, String password);
     public void createUser(String username, String password, String role);
}