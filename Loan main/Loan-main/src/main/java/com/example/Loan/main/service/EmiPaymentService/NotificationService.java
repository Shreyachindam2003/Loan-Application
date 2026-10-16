package com.example.Loan.main.service.EmiPaymentService;

import java.util.List;

import com.example.Loan.main.dto.EmiPaymentDto.response.NotificationResponse;

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
}
