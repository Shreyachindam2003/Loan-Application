
package com.example.Loan.main.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Loan.main.config.ApiResponse;
import com.example.Loan.main.dto.response.NotificationResponse;
import com.example.Loan.main.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(@PathVariable Integer customerId) {

        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                        .success(true)
                        .message("Notifications fetched successfully")
                        .data(notificationService.getNotifications(customerId))
                        .build()
        );
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Integer notificationId) {

        notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                        .success(true)
                        .message("Notification marked as read successfully")
                        .data(null)
                        .build()
        );
    }

    @GetMapping("/customer/{customerId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @PathVariable Integer customerId) {

        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                        .success(true)
                        .message("Unread notifications fetched successfully")
                        .data(notificationService.getUnreadNotifications(customerId))
                        .build()
        );
    }
}
