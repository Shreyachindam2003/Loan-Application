package com.example.Loan.main.entity.ticketnotification;

import com.example.Loan.main.enums.ticketnotification.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationId")
    private Integer notificationId;

    // Kept for compatibility with the existing Notifications table.
    // Used when the notification is for a customer.
    @Column(name = "CustomerId")
    private Integer customerId;

    // Used when the notification is for a Loan Officer/User.
    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "Message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "NotificationType", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(name = "IsRead", nullable = false)
    private Boolean isRead = false;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
