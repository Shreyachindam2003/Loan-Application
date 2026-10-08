package com.example.loanapplication.entity.EmiPaymentEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.loanapplication.enums.EmiPaymentEnums.EmiStatus;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "EmiSchedules")
@Data
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EmiScheduleId")
    private Integer emiScheduleId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "InstallmentNo", nullable = false)
    private Integer installmentNo;

    @Column(name = "DueDate", nullable = false)
    private LocalDate dueDate;

    @Column(name = "PrincipalAmount", nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "InterestAmount", nullable = false)
    private BigDecimal interestAmount;

    @Column(name = "OpeningBalance", nullable = false)
    private BigDecimal openingBalance;

    @Column(name = "ClosingBalance", nullable = false)
    private BigDecimal closingBalance;

    @Column(name = "Emi", nullable = false)
    private BigDecimal emiAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentStatus", nullable = false)
    private EmiStatus status;

    @Column(name = "PaidDate")
    private LocalDate paidDate;

    @Column(name = "CancellationReason")
    private String cancellationReason;
}