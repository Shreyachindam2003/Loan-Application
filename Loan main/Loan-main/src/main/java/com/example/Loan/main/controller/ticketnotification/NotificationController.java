package com.example.Loan.main.controller.ticketnotification;

import com.example.Loan.main.dto.ticketnotification.NotificationResponse;
import com.example.Loan.main.service.ticketnotification.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Loan Officer/User notifications
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = createPageable(page, size);
        List<NotificationResponse> all = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(toPage(all, pageable));
    }

    // Customer notifications
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<NotificationResponse>> getCustomerNotifications(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = createPageable(page, size);
        List<NotificationResponse> all = notificationService.getCustomerNotifications(customerId);
        return ResponseEntity.ok(toPage(all, pageable));
    }

    private Page<NotificationResponse> toPage(List<NotificationResponse> all, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<NotificationResponse> content =
                start >= all.size() ? List.of() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be 0 or greater");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        return PageRequest.of(page, size);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Integer notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Integer notificationId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer customerId) {

        notificationService.deleteNotification(
                notificationId,
                userId,
                customerId
        );

        return ResponseEntity.noContent().build();
    }

}
