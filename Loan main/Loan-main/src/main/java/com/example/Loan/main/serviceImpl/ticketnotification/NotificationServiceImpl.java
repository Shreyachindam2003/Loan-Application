package com.example.Loan.main.serviceImpl.ticketnotification;

import com.example.Loan.main.dto.ticketnotification.NotificationResponse;
import com.example.Loan.main.entity.ticketnotification.Notification;
import com.example.Loan.main.exception.ResourceNotFoundException;
import com.example.Loan.main.respository.ticketnotification.NotificationRepository;
import com.example.Loan.main.service.ticketnotification.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            ModelMapper modelMapper) {
        this.notificationRepository = notificationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<NotificationResponse> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification ->
                        modelMapper.map(notification, NotificationResponse.class))
                .toList();
    }

    @Override
    public List<NotificationResponse> getCustomerNotifications(Integer customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(notification ->
                        modelMapper.map(notification, NotificationResponse.class))
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with ID: " + notificationId
                        ));

        notification.setIsRead(true);

        return modelMapper.map(
                notificationRepository.save(notification),
                NotificationResponse.class
        );
    }

    @Override
    public void deleteNotification(
            Integer notificationId,
            Integer userId,
            Integer customerId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with ID: " + notificationId
                        ));

        // Verify that the notification belongs to the requesting user/customer.
        boolean belongsToUser = userId != null
                && notification.getUserId() != null
                && notification.getUserId().equals(userId);

        boolean belongsToCustomer = customerId != null
                && notification.getCustomerId() != null
                && notification.getCustomerId().equals(customerId);

        if (!belongsToUser && !belongsToCustomer) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You are not allowed to delete this notification"
            );
        }

        notificationRepository.delete(notification);
    }
}
