package com.example.Loan.main.controller;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Loan.main.dto.NotificationResponse;
import com.example.Loan.main.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(
                notificationService.getNotifications(customerId));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Integer notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customer/{customerId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @PathVariable Integer customerId) {

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(customerId));
    }
}