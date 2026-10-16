package com.example.Loan.main.dto;

import com.example.Loan.main.enums.NotificationType;

import lombok.Data;

@Data

public class NotificationCreateRequest {

    private Integer customerId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private Integer referenceId;
}