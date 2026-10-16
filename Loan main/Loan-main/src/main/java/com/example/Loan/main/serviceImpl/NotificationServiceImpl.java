package com.example.Loan.main.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Loan.main.dto.response.NotificationResponse;
import com.example.Loan.main.entity.Notification;
import com.example.Loan.main.enums.NotificationType;
import com.example.Loan.main.exception.NotificationNotFoundException;
import com.example.Loan.main.respository.NotificationRepository;
import com.example.Loan.main.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final ModelMapper modelMapper;

    // Get all notifications
    @Override
    public List<NotificationResponse> getNotifications(Integer customerId) {

        return notificationRepo
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(notification ->
                        modelMapper.map(notification, NotificationResponse.class))
                .toList();
    }

    // Get unread notifications
    public List<NotificationResponse> getUnreadNotifications(
            Integer customerId) {

        return notificationRepo
                .findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId)
                .stream()
                .map(notification ->
                        modelMapper.map(notification, NotificationResponse.class))
                .toList();
    }

    // Mark notification as read
    @Override
    @Transactional
    public void markAsRead(Integer notificationId) {

        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found: " + notificationId));

        notification.setIsRead(true);
        notificationRepo.save(notification);
    }

    // Create EMI reminder once per EMI per day
    @Override
    @Transactional
    public boolean createEmiReminder(
            Integer customerId,
            String title,
            String message,
            Integer emiScheduleId) {

        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        if (emiScheduleId == null || emiScheduleId <= 0) {
            throw new IllegalArgumentException("Invalid EMI schedule ID");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Reminder message is required");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        boolean alreadyExists = notificationRepo
                .findFirstByCustomerIdAndReferenceIdAndNotificationTypeAndCreatedAtBetween(
                        customerId,
                        emiScheduleId,
                        NotificationType.EMI_REMINDER,
                        start,
                        end)
                .isPresent();

        if (alreadyExists) {
            return false;
        }

        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setTitle(
                title == null || title.isBlank()
                        ? "EMI Payment Reminder"
                        : title);
        notification.setMessage(message);
        notification.setNotificationType(NotificationType.EMI_REMINDER);
        notification.setReferenceId(emiScheduleId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepo.save(notification);

        return true;
    }

    @Override
    @Transactional
    public boolean createOverdueWarning(
            Integer customerId,
            String title,
            String message,
            Integer emiScheduleId) {

        boolean exists = notificationRepo
                .existsByCustomerIdAndReferenceIdAndNotificationType(
                        customerId,
                        emiScheduleId,
                        NotificationType.OVERDUE_EMI);

        if (exists) {
            return false;
        }

        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(NotificationType.OVERDUE_EMI);
        notification.setReferenceId(emiScheduleId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepo.save(notification);
        return true;
    }
}
