package com.user.dto;

import jakarta.validation.constraints.Pattern;

public record LoginDTO (
    String username,
    @Pattern(regexp = "[a-zA-Z0-9 ]+")
    String password
) {}