package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Loan.main.dto.EmiPaymentDto.response.NotificationResponse;
import com.example.Loan.main.entity.EmiPaymentEntity.Notification;
import com.example.Loan.main.enums.EmiPaymentEnums.NotificationType;
import com.example.Loan.main.exception.InvalidNotificationTypeException;
import com.example.Loan.main.exception.NotificationNotFoundException;
import com.example.Loan.main.respository.EmiPaymentRepo.NotificationRepository;
import com.example.Loan.main.service.EmiPaymentService.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final ModelMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Integer customerId) {

        log.info("Fetching notifications for Customer ID: {}", customerId);

        List<NotificationResponse> response = notificationRepo
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(n -> mapper.map(n, NotificationResponse.class))
                .toList();

        log.info("Notifications fetched. Customer ID: {}, Count: {}",
                customerId, response.size());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(
            Integer customerId) {

        log.info("Fetching unread notifications for Customer ID: {}",
                customerId);

        List<NotificationResponse> response = notificationRepo
                .findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId)
                .stream()
                .map(n -> mapper.map(n, NotificationResponse.class))
                .toList();

        log.info("Unread notifications fetched. Customer ID: {}, Count: {}",
                customerId, response.size());

        return response;
    }

    @Override
    @Transactional
    public void markAsRead(Integer notificationId) {

        log.info("Marking notification as read. Notification ID: {}",
                notificationId);

        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> {
                    log.warn("Notification not found. ID: {}", notificationId);
                    return new NotificationNotFoundException(
                            "Notification not found: " + notificationId);
                });

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notificationRepo.save(notification);
        }

        log.info("Notification marked as read. ID: {}", notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Integer customerId) {

        log.info("Marking all unread notifications as read. Customer ID: {}",
                customerId);

        List<Notification> notifications = notificationRepo
                .findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId);

        notifications.forEach(n -> n.setIsRead(true));
        notificationRepo.saveAll(notifications);

        log.info("Notifications marked as read. Customer ID: {}, Count: {}",
                customerId, notifications.size());
    }

    @Override
    @Transactional
    public void createNotification(
            Integer customerId,
            String title,
            String message,
            String notificationType,
            Integer referenceId) {

        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException(
                    "Customer ID must be a positive number");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification message cannot be empty");
        }

        log.info("Creating notification. Customer ID: {}, Type: {}, Reference ID: {}",
                customerId, notificationType, referenceId);

        NotificationType type = parseNotificationType(notificationType);

        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepo.save(notification);

        log.info("Notification created successfully. Customer ID: {}, Type: {}, Reference ID: {}",
                customerId, type, referenceId);
    }

    @Override
    public void sendEmail(String email, String subject, String message) {

        log.info("Email sending requested. Recipient: {}, Subject: {}",
                email, subject);

        // Email implementation will be added later.
        log.debug("Email sending is not implemented yet.");
    }

    @Override
    @Transactional
    public void createEmiReminder(
            Integer customerId,
            String title,
            String message,
            Integer emiScheduleId) {

        if (customerId == null || customerId <= 0
                || emiScheduleId == null || emiScheduleId <= 0) {
            throw new IllegalArgumentException(
                    "Customer ID and EMI Schedule ID must be positive numbers");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "EMI reminder message cannot be empty");
        }

        log.info("Creating EMI reminder. Customer ID: {}, EMI ID: {}",
                customerId, emiScheduleId);

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        boolean alreadyExists = notificationRepo
                .findFirstByCustomerIdAndReferenceIdAndNotificationTypeAndCreatedAtBetween(
                        customerId,
                        emiScheduleId,
                        NotificationType.EMI_REMINDER,
                        start,
                        end)
                .isPresent();

        if (alreadyExists) {
            log.info("Duplicate EMI reminder skipped. Customer ID: {}, EMI ID: {}",
                    customerId, emiScheduleId);
            return;
        }

        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(NotificationType.EMI_REMINDER);
        notification.setReferenceId(emiScheduleId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepo.save(notification);

        log.info("EMI reminder created successfully. Customer ID: {}, EMI ID: {}",
                customerId, emiScheduleId);
    }

    private NotificationType parseNotificationType(String notificationType) {

        try {
            return NotificationType.valueOf(notificationType);
        } catch (IllegalArgumentException | NullPointerException ex) {
            log.warn("Invalid notification type received: {}", notificationType);

            throw new InvalidNotificationTypeException(
                    "Invalid notification type: " + notificationType);
        }
    }
}
