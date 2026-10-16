package com.example.Loan.main.service;

import java.util.List;

import com.example.Loan.main.dto.NotificationResponse;
import com.example.Loan.main.enums.NotificationType;

public interface NotificationService {

    List<NotificationResponse> getNotifications(Integer customerId);

    void markAsRead(Integer notificationId);

    boolean createEmiReminder(
            Integer customerId,
            String title,
            String message,
            Integer emiScheduleId);

    boolean createOverdueWarning(
            Integer customerId,
            String title,
            String message,
            Integer emiScheduleId);

    List<NotificationResponse> getUnreadNotifications(Integer customerId);



    boolean createNotification(
            Integer customerId,
            String title,
            String message,
            NotificationType notificationType,
            Integer referenceId
    );
}