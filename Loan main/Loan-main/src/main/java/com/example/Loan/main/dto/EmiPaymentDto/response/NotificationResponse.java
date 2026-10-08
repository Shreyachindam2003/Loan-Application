package com.example.Loan.main.dto.EmiPaymentDto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Integer notificationId;

    private String message;

    private Boolean isRead;

    private String notificationType;

    private Integer referenceId;

    private LocalDateTime createdAt;
}
