package com.example.loanapplication.dto.auth;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        String twoFactorCode
) {}
