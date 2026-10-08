package com.example.loanapplication.serviceImpl.auth;

import com.example.loanapplication.service.auth.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(fromEmail);
        message.setSubject("Loan Application - Email Verification OTP");
        message.setText(
                "Hello,\n\n" +
                "Your Loan Application email verification OTP is: " + otp +
                "\n\nThis OTP is valid for 10 minutes." +
                "\n\nPlease do not share this OTP with anyone." +
                "\n\nRegards,\nLoan Application Team"
        );
        mailSender.send(message);
    }
}
