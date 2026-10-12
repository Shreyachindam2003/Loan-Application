package com.example.Loan.main.entity.EmiPaymentEntity;

import java.time.LocalDateTime;


import com.example.Loan.main.enums.EmiPaymentEnums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationId")
    private Integer notificationId;

    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "Title")
    private String title;

    @Column(name = "Message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "NotificationType")
    private NotificationType notificationType;

    @Column(name = "ReferenceId")
    private Integer referenceId;

    @Column(name = "IsRead")
    private Boolean isRead;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}
