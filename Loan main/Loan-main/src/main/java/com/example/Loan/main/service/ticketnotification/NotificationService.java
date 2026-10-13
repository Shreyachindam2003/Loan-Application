package com.example.Loan.main.service.ticketnotification;

import com.example.Loan.main.dto.ticketnotification.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getUserNotifications(Integer userId);

    List<NotificationResponse> getCustomerNotifications(Integer customerId);

    NotificationResponse markAsRead(Integer notificationId);
}
