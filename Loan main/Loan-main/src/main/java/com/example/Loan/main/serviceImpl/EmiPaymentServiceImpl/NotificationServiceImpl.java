package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.util.List;

import com.example.Loan.main.dto.EmiPaymentDto.response.NotificationResponse;
import com.example.Loan.main.entity.EmiPaymentEntity.Notification;
import com.example.Loan.main.enums.EmiPaymentEnums.NotificationType;
import com.example.Loan.main.exception.EmiPaymentException.ResourceNotFoundException;
import com.example.Loan.main.respository.EmiPaymentRepo.NotificationRepository;
import com.example.Loan.main.service.EmiPaymentService.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly=true)
    public Page<NotificationResponse> getNotifications(Integer customerId,Pageable pageable) {
        Page<Notification> notifications=notificationRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId,pageable);

        return notifications.map(this::mapResponse);
    }

    @Override
    @Transactional(readOnly=true)
    public List<NotificationResponse> getUnreadNotifications(Integer customerId) {
        return notificationRepository
                .findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    @Override
    public void markAsRead(Integer notificationId) {
        Notification notification=notificationRepository.findById(notificationId)
                .orElseThrow(()->new ResourceNotFoundException("Notification not found"));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Integer customerId) {
        List<Notification> notifications=notificationRepository
                .findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId);

        for(Notification notification:notifications) {
            notification.setIsRead(true);
        }

        notificationRepository.saveAll(notifications);
    }

    @Override
    public void createNotification(Integer customerId,String message
            ,NotificationType notificationType,Integer referenceId) {
        Notification notification=new Notification();

        notification.setCustomerId(customerId);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setNotificationType(notificationType.name());
        notification.setReferenceId(referenceId);

        notificationRepository.save(notification);
    }

    private NotificationResponse mapResponse(Notification notification) {
        return modelMapper.map(notification,NotificationResponse.class);
    }
}