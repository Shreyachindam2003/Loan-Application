package com.example.loanapplication.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Loan Application - Email Verification OTP");

        message.setText(
                "Your OTP is: " + otp +
                        "\n\nThis OTP is valid for 10 minutes."
        );

        mailSender.send(message);
    }
}