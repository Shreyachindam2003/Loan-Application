package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.CustomerNotificationResponse;
import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.service.loandealcreation.CustomerNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/notifications")
@RequiredArgsConstructor
public class CustomerNotificationController {

    private final CustomerNotificationService service;

    @GetMapping
    public ResponseApi<List<CustomerNotificationResponse>> getNotifications(
            @PathVariable("customerId") Integer customerId
    ) {
        return ResponseApi.success(
                "Customer notifications fetched",
                service.getNotifications(customerId)
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseApi<Void> markRead(
            @PathVariable("customerId") Integer customerId,
            @PathVariable("notificationId") Integer notificationId
    ) {
        service.markRead(customerId, notificationId);

        return ResponseApi.success(
                "Notification marked as read",
                null
        );
    }
}