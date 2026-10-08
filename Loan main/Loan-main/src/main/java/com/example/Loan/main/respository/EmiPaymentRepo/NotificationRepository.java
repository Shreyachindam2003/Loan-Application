package com.example.Loan.main.respository.EmiPaymentRepo;


import com.example.Loan.main.entity.EmiPaymentEntity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Integer> {

    Page<Notification> findByCustomerIdOrderByCreatedAtDesc(
            Integer customerId,
            Pageable pageable
    );

    List<Notification> findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(
            Integer customerId
    );

    long countByCustomerIdAndIsReadFalse(
            Integer customerId
    );
}