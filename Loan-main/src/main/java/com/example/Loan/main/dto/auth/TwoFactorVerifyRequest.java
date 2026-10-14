package com.example.Loan.main.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorVerifyRequest(@NotBlank String code) {}
