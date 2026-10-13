package com.example.Loan.main.dto.loandealcreation.response;

import java.time.LocalDateTime;

public record CustomerNotificationResponse(
        Integer notificationId,
        Integer customerId,
        String message,
        boolean read,
        LocalDateTime createdAt
) {
}
//dada