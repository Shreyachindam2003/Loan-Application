package com.example.Loan.main.service.loandealcreation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        name = "app.email.enabled",
        havingValue = "true"
)
public class LoanDecisionEmailWorker {

    private final JdbcTemplate jdbc;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public LoanDecisionEmailWorker(
            JdbcTemplate jdbc,
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromAddress
    ) {
        this.jdbc = jdbc;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Scheduled(
            initialDelay = 10000,
            fixedDelayString = "${app.email.poll-ms:10000}"
    )
    public void sendPendingEmails() {
        List<QueuedEmail> emails = jdbc.query("""
                SELECT EmailId, Recipient, Subject, Body
                FROM LoanDecisionEmails
                WHERE Status IN ('PENDING', 'SENDING')
                  AND NextAttemptAt <= CURRENT_TIMESTAMP(6)
                ORDER BY EmailId
                LIMIT 10
                """,
                (rs, row) -> new QueuedEmail(
                        rs.getLong("EmailId"),
                        rs.getString("Recipient"),
                        rs.getString("Subject"),
                        rs.getString("Body")
                )
        );

        for (QueuedEmail email : emails) {
            int claimed = jdbc.update("""
                    UPDATE LoanDecisionEmails
                    SET Status = 'SENDING',
                        Attempts = Attempts + 1,
                        NextAttemptAt = DATE_ADD(
                            CURRENT_TIMESTAMP(6), INTERVAL 5 MINUTE
                        )
                    WHERE EmailId = ?
                      AND Status IN ('PENDING', 'SENDING')
                      AND NextAttemptAt <= CURRENT_TIMESTAMP(6)
                    """, email.id());

            if (claimed != 1) {
                continue;
            }

            try {
                SimpleMailMessage message = new SimpleMailMessage();

                message.setFrom(fromAddress);
                message.setTo(email.recipient());
                message.setSubject(email.subject());
                message.setText(email.body());

                mailSender.send(message);

                jdbc.update("""
                        UPDATE LoanDecisionEmails
                        SET Status = 'SENT',
                            SentAt = CURRENT_TIMESTAMP(6)
                        WHERE EmailId = ?
                        """, email.id());

            } catch (Exception exception) {
                jdbc.update("""
                        UPDATE LoanDecisionEmails
                        SET Status = 'PENDING',
                            NextAttemptAt = DATE_ADD(
                                CURRENT_TIMESTAMP(6), INTERVAL 1 MINUTE
                            )
                        WHERE EmailId = ?
                        """, email.id());
            }
        }
    }

    private record QueuedEmail(
            long id,
            String recipient,
            String subject,
            String body
    ) {
    }
}//dada