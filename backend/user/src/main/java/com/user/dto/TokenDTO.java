package com.user.dto;

import java.util.List;

public record TokenDTO (
    String username,
    String token, 
    List<Integer> pin
) {}