package com.example.Loan.main.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CustomerNotifications")
public class CustomerNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationId")
    private Integer notificationId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "Title", nullable = false, length = 150)
    private String title;

    @Column(name = "Message", nullable = false, length = 1000)
    private String message;

    @Column(name = "IsRead", nullable = false)
    private boolean read = false;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}