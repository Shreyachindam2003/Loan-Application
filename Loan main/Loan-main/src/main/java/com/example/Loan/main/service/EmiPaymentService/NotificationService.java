package com.example.Loan.main.service.EmiPaymentService;


import com.example.Loan.main.dto.EmiPaymentDto.response.NotificationResponse;

import java.util.List;


public interface NotificationService {

    List<NotificationResponse> getNotifications(Integer customerId);

    List<NotificationResponse> getUnreadNotifications(Integer customerId);

    void markAsRead(Integer notificationId);

    void markAllAsRead(Integer customerId);

    void createNotification(
            Integer customerId,
            String title,
            String message,
            String notificationType,
            Integer referenceId);

    void sendEmail(
            String email,
            String subject,
            String message);

    void createEmiReminder(
            Integer customerId,
            String title,
            String message,
            Integer emiScheduleId);
}
