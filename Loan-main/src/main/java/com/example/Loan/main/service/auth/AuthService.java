package com.example.Loan.main.service.auth;


import com.example.Loan.main.dto.auth.*;
import com.example.Loan.main.entity.auth.User;
import jakarta.transaction.Transactional;

public interface AuthService {
    MessageResponse register(RegisterRequest request);
    MessageResponse verifyEmail(VerifyOtpRequest request);
    MessageResponse resendOtp(String email);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String rawRefreshToken);
    MessageResponse logout(String refreshToken);
    String setup2fa(String email);
    MessageResponse enable2fa(String email, String code);
    MessageResponse disable2fa(String email, String code);
    AuthResponse oauthLogin(String email);
    AuthResponse verifyOAuth2Factor(String email, String code);

    @Transactional
    AuthResponse issueTokens(User user);
}
