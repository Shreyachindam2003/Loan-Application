package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.CustomerNotificationResponse;

import java.util.List;

public interface CustomerNotificationService {

    List<CustomerNotificationResponse> getNotifications(
            Integer customerId
    );

    void markRead(
            Integer customerId,
            Integer notificationId
    );
}//dada