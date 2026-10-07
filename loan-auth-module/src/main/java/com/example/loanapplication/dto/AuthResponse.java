package com.example.loanapplication.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        boolean twoFactorRequired,
        String message
) {}
