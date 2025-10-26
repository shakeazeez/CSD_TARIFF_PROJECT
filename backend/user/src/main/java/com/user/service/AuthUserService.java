package com.user.service;

import com.user.dto.CreateUserDTO;
import com.user.dto.TokenDTO;

public interface AuthUserService{
     public TokenDTO createUser(CreateUserDTO createUserDTO);
}