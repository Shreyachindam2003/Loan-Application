package com.example.Loan.main.dto.response;


import java.time.LocalDateTime;


import com.example.Loan.main.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationResponse {

    private Integer notificationId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private Integer referenceId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}