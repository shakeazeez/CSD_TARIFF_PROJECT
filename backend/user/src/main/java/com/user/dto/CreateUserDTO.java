package com.user.dto;

public record CreateUserDTO (
    String username, 
    String password, 
    String role
) {}