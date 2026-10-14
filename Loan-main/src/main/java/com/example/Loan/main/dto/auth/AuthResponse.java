package com.example.Loan.main.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        boolean twoFactorRequired,
        String message
) {}
