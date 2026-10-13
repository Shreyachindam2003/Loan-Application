package com.example.Loan.main.controller.EmiPaymentController;

import java.util.List;

import com.example.Loan.main.dto.EmiPaymentDto.response.ApiResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.NotificationResponse;
import com.example.Loan.main.service.EmiPaymentService.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    // Get all notifications
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @PathVariable Integer customerId) {

        log.info("Fetching notifications for customerId: {}", customerId);

        List<NotificationResponse> data =
                notificationService.getNotifications(customerId);

        log.info("Notifications fetched successfully for customerId: {}, count: {}",
                customerId, data.size());

        return ResponseEntity.ok(
                ApiResponse.<List<NotificationResponse>>builder()
                        .success(true)
                        .message("Notifications fetched successfully")
                        .data(data)
                        .build()
        );
    }

    // Get unread notifications
    @GetMapping("/customer/{customerId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @PathVariable Integer customerId) {

        log.info("Fetching unread notifications for customerId: {}", customerId);

        List<NotificationResponse> data =
                notificationService.getUnreadNotifications(customerId);

        log.info("Unread notifications fetched successfully for customerId: {}, count: {}",
                customerId, data.size());

        return ResponseEntity.ok(
                ApiResponse.<List<NotificationResponse>>builder()
                        .success(true)
                        .message("Unread notifications fetched successfully")
                        .data(data)
                        .build()
        );
    }

    // Mark one notification as read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Integer notificationId) {

        log.info("Marking notification as read, notificationId: {}",
                notificationId);

        notificationService.markAsRead(notificationId);

        log.info("Notification marked as read successfully, notificationId: {}",
                notificationId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Notification marked as read")
                        .data(null)
                        .build()
        );
    }

    // Mark all notifications as read
    @PutMapping("/customer/{customerId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @PathVariable Integer customerId) {

        log.info("Marking all notifications as read for customerId: {}",
                customerId);

        notificationService.markAllAsRead(customerId);

        log.info("All notifications marked as read successfully for customerId: {}",
                customerId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("All notifications marked as read")
                        .data(null)
                        .build()
        );
    }
}

