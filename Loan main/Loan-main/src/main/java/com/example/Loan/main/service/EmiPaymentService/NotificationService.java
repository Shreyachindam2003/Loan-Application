package com.example.Loan.main.service.EmiPaymentService;

import com.example.Loan.main.dto.EmiPaymentDto.response.NotificationResponse;
import com.example.Loan.main.enums.EmiPaymentEnums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    Page<NotificationResponse> getNotifications(
            Integer customerId,
            Pageable pageable
    );

    List<NotificationResponse> getUnreadNotifications(Integer customerId);

    void markAsRead(Integer notificationId);

    void markAllAsRead(Integer customerId);

    void createNotification(
            Integer customerId,
            String message,
            NotificationType notificationType,
            Integer referenceId
    );
}
