package com.example.loanapplication.entity.EmiPaymentEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "LoanDeals")
@Data
public class LoanDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DealId")
    private Integer dealId;

    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "LoanAmount")
    private BigDecimal loanAmount;

    @Column(name = "InterestRate")
    private BigDecimal interestRate;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(name = "DealStatus")
    private String dealStatus;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}