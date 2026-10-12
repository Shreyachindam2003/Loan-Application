package com.example.Loan.main.respository.EmiPaymentRepo;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.Loan.main.entity.EmiPaymentEntity.Notification;
import com.example.Loan.main.enums.EmiPaymentEnums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepository
        extends JpaRepository<Notification, Integer> {

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(
            Integer customerId);

    List<Notification> findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(
            Integer customerId);

    Optional<Notification> findFirstByCustomerIdAndReferenceIdAndNotificationTypeAndCreatedAtBetween(
            Integer customerId,
            Integer referenceId,
            NotificationType notificationType,
            LocalDateTime start,
            LocalDateTime end);


}