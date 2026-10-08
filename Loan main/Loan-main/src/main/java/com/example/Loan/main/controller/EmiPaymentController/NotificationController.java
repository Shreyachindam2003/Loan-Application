package com.example.Loan.main.controller.EmiPaymentController;

import com.example.Loan.main.dto.EmiPaymentDto.response.ApiResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.NotificationResponse;
import com.example.Loan.main.service.EmiPaymentService.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    // =========================================================
    // 1. GET ALL NOTIFICATIONS
    // GET /api/v1/notifications/customer/101?page=0&size=10
    // =========================================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<NotificationResponse> response =
                notificationService.getNotifications(
                        customerId,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.<Page<NotificationResponse>>builder()
                        .success(true)
                        .message("Notifications fetched successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 2. GET UNREAD NOTIFICATIONS
    // GET /api/v1/notifications/customer/101/unread
    // =========================================================

    @GetMapping("/customer/{customerId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @PathVariable Integer customerId) {

        List<NotificationResponse> response =
                notificationService.getUnreadNotifications(
                        customerId
                );

        return ResponseEntity.ok(
                ApiResponse.<List<NotificationResponse>>builder()
                        .success(true)
                        .message("Unread notifications fetched successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 3. MARK ONE NOTIFICATION AS READ
    // PUT /api/v1/notifications/1/read
    // =========================================================

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Integer notificationId) {

        notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Notification marked as read")
                        .data(null)
                        .build()
        );
    }


    // =========================================================
    // 4. MARK ALL NOTIFICATIONS AS READ
    // PUT /api/v1/notifications/customer/101/read-all
    // =========================================================

    @PutMapping("/customer/{customerId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @PathVariable Integer customerId) {

        notificationService.markAllAsRead(customerId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("All notifications marked as read")
                        .data(null)
                        .build()
        );
    }
}
