package com.example.Loan.main.respository;

import com.example.Loan.main.entity.CustomerNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerNotificationRepository
        extends JpaRepository<CustomerNotification, Integer> {

    List<CustomerNotification> findByCustomerIdOrderByCreatedAtDesc(
            Integer customerId
    );
}