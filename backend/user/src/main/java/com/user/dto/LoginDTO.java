package com.user.dto;

import jakarta.validation.constraints.Pattern;

public record LoginDTO (
    String username,
    String password
) {}