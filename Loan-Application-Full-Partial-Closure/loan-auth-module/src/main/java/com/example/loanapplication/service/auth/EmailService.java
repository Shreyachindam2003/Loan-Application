package com.example.loanapplication.service.auth;

public interface EmailService {
    void sendOtpEmail(String email, String otp);
}
