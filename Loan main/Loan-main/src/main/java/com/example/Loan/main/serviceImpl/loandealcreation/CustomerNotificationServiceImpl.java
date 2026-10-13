package com.example.Loan.main.serviceImpl.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.CustomerNotificationResponse;
import com.example.Loan.main.exception.loandealcreation.ResourceNotFoundException;
import com.example.Loan.main.service.loandealcreation.CustomerNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerNotificationServiceImpl
        implements CustomerNotificationService {

    private final JdbcTemplate jdbc;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerNotificationResponse> getNotifications(
            Integer customerId
    ) {
        return jdbc.query("""
                SELECT NotificationId, CustomerId,
                       Message, IsRead, CreatedAt
                FROM Notifications
                WHERE CustomerId = ?
                ORDER BY CreatedAt DESC, NotificationId DESC
                """,
                (rs, row) -> {
                    Timestamp createdAt = rs.getTimestamp("CreatedAt");

                    return new CustomerNotificationResponse(
                            rs.getInt("NotificationId"),
                            rs.getInt("CustomerId"),
                            rs.getString("Message"),
                            rs.getBoolean("IsRead"),
                            createdAt == null
                                    ? null
                                    : createdAt.toLocalDateTime()
                    );
                },
                customerId
        );
    }

    @Override
    @Transactional
    public void markRead(
            Integer customerId,
            Integer notificationId
    ) {
        List<Boolean> notifications = jdbc.query("""
                SELECT IsRead
                FROM Notifications
                WHERE NotificationId = ?
                  AND CustomerId = ?
                FOR UPDATE
                """,
                (rs, row) -> rs.getBoolean("IsRead"),
                notificationId,
                customerId
        );

        if (notifications.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Customer notification not found"
            );
        }

        if (!notifications.get(0)) {
            jdbc.update("""
                    UPDATE Notifications
                    SET IsRead = TRUE
                    WHERE NotificationId = ?
                      AND CustomerId = ?
                    """,
                    notificationId,
                    customerId
            );
        }
    }
}