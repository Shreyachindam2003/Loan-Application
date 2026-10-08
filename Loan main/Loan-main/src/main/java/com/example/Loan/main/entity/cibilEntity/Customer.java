package com.example.Loan.main.entity.cibilEntity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Customers")
 @Data
@RequiredArgsConstructor

public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Long   customerId;

    @Column(name = "FirstName", nullable = false)
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Password")
    private String password;

    @Column(name = "MobileNo")
    private String mobileNo;

    @Column(name = "AadhaarNo")
    private String aadhaarNo;

    @Column(name = "PanNo")
    private String panNo;

    @Column(name = "Age")
    private Integer age;

    @Column(name = "EmploymentType")
    private String employmentType;

    @Column(name = "MonthlyIncome")
    private BigDecimal monthlyIncome;

    @Column(name = "MonthlyInvestment")
    private BigDecimal monthlyInvestment;

    @Column(name = "IsEmailVerified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "VerificationOtpHash")
    private String verificationOtpHash;

    @Column(name = "VerificationOtpExpiry")
    private LocalDateTime verificationOtpExpiry;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        if (emailVerified == null) emailVerified = false;
    }
}
