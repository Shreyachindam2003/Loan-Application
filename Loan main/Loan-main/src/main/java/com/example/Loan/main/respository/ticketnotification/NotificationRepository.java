package com.example.Loan.main.respository.ticketnotification;

import com.example.Loan.main.entity.ticketnotification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);
}
