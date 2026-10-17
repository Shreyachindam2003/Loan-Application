package com.example.Loan.main.service;

public interface EmailService {

    void sendOtpEmail(String email, String otp);

    void sendClosureStatusEmail(
            String email,
            Integer requestId,
            Integer loanAccountId,
            String closureType,
            String status
    );
}