package com.example.loanapplication.entity.closure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "LoanPayments")
@Data
public class LoanPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentId")
    private Integer paymentId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "PaymentAmount", nullable = false)
    private BigDecimal paymentAmount;

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @Column(name = "PaymentStatus")
    private String paymentStatus;

    @Column(name = "PaymentName")
    private String paymentName;
}