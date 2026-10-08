package com.example.loanapplication.service.auth;

import com.example.loanapplication.dto.auth.AuthResponse;
import com.example.loanapplication.dto.auth.LoginRequest;
import com.example.loanapplication.dto.auth.MessageResponse;
import com.example.loanapplication.dto.auth.RegisterRequest;
import com.example.loanapplication.dto.auth.VerifyOtpRequest;
import com.example.loanapplication.entity.auth.User;
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

    @Transactional
    AuthResponse issueTokens(User user);
}
