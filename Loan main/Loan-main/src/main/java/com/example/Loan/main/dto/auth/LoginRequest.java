package com.example.Loan.main.dto.auth;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        String twoFactorCode
) {}
