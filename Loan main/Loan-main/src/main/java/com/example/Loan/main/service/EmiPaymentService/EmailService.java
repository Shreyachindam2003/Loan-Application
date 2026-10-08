package com.example.Loan.main.service.EmiPaymentService;

public interface EmailService {

    void sendEmail(
            String toEmail,
            String subject,
            String message
    );
}
