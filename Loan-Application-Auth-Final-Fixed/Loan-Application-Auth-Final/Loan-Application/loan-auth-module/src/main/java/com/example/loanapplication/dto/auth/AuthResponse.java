package com.example.loanapplication.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        boolean twoFactorRequired,
        String message
) {}
