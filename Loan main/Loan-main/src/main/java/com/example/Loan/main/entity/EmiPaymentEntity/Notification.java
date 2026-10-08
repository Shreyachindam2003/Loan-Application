package com.example.Loan.main.entity.EmiPaymentEntity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "Notifications",
        indexes = {
                @Index(
                        name = "IX_Notifications_CustomerId",
                        columnList = "CustomerId"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationId")
    private Integer notificationId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "Message", nullable = false, length = 1000)
    private String message;

    @Column(name = "IsRead", nullable = false)
    private Boolean isRead = false;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "NotificationType", length = 50)
    private String notificationType;

    @Column(name = "ReferenceId")
    private Integer referenceId;
}