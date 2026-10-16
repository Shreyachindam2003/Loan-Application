package com.example.Loan.main.respository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Loan.main.entity.Notification;
import com.example.Loan.main.enums.NotificationType;

public interface NotificationRepository
        extends JpaRepository<Notification, Integer> {

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(
            Integer customerId);

    List<Notification> findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(
            Integer customerId);

    Optional<Notification>
    findFirstByCustomerIdAndReferenceIdAndNotificationTypeAndCreatedAtBetween(
            Integer customerId,
            Integer referenceId,
            NotificationType notificationType,
            LocalDateTime start,
            LocalDateTime end);

    boolean existsByCustomerIdAndReferenceIdAndNotificationType(
            Integer customerId,
            Integer referenceId,
            NotificationType notificationType);


}