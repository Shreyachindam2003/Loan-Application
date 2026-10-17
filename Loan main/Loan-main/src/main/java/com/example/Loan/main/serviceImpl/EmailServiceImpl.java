package com.example.Loan.main.serviceImpl;

import com.example.Loan.main.service.EmailService;
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

    @Override
    public void sendClosureStatusEmail(
            String email,
            Integer requestId,
            Integer loanAccountId,
            String closureType,
            String status) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setFrom(fromEmail);
        message.setSubject("Loan Application - Closure Request Update");

        String statusMessage = "APPROVED".equalsIgnoreCase(status)
                ? "Your closure request has been approved."
                : "Your closure request has been rejected.";

        message.setText(
                "Hello,\n\n" +
                        "Your loan closure request has been reviewed.\n\n" +
                        "Request ID: " + requestId + "\n" +
                        "Loan Account ID: " + loanAccountId + "\n" +
                        "Closure Type: " + closureType + "\n" +
                        "Status: " + status + "\n\n" +
                        statusMessage + "\n\n" +
                        "Please sign in to LoanApp to check your request details.\n\n" +
                        "Regards,\nLoan Application Team"
        );

        mailSender.send(message);
    }
}