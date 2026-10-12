package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;


import com.example.Loan.main.service.EmiPaymentService.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {

        if (!StringUtils.hasText(to)
                || !StringUtils.hasText(subject)
                || !StringUtils.hasText(body)) {
            throw new IllegalArgumentException(
                    "Email, subject and body are required");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        log.info("Email sent successfully to {}", to);
    }
}
