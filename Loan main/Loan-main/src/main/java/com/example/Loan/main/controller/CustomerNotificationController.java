package com.example.Loan.main.controller;

import com.example.Loan.main.entity.Customer;
import com.example.Loan.main.entity.CustomerNotification;
import com.example.Loan.main.respository.CustomerNotificationRepository;
import com.example.Loan.main.respository.CustomerRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class CustomerNotificationController {

    private final CustomerRepository customerRepository;
    private final CustomerNotificationRepository notificationRepository;

    public CustomerNotificationController(
            CustomerRepository customerRepository,
            CustomerNotificationRepository notificationRepository) {

        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/me")
    public List<CustomerNotification> getMyNotifications(
            Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {
            throw new EntityNotFoundException(
                    "Authenticated customer not found");
        }

        Customer customer = customerRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Customer not found for authenticated account"));

        return notificationRepository
                .findByCustomerIdOrderByCreatedAtDesc(
                        customer.getCustomerId());
    }
}