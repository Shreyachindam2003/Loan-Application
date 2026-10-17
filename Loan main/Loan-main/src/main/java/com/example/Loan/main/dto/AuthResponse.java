package com.example.Loan.main.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        boolean twoFactorRequired,
        String message,
        String twoFactorSetupUri,
        String role
) {
    // Backward-compatible constructors keep all existing response creation sites working.
    public AuthResponse(String accessToken, String refreshToken,
                        boolean twoFactorRequired, String message,
                        String twoFactorSetupUri) {
        this(accessToken, refreshToken, twoFactorRequired, message, twoFactorSetupUri, null);
    }

    public AuthResponse(String accessToken, String refreshToken,
                        boolean twoFactorRequired, String message) {
        this(accessToken, refreshToken, twoFactorRequired, message, null, null);
    }
}
