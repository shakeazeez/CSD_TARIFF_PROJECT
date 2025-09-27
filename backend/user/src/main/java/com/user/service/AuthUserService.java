package com.user.service;

import com.user.dto.CreateUserDTO;
import com.user.dto.LoginDTO;
import com.user.dto.TokenDTO;

public interface AuthUserService{
     public TokenDTO login(LoginDTO loginDTO);
     public void createUser(CreateUserDTO createUserDTO);
}