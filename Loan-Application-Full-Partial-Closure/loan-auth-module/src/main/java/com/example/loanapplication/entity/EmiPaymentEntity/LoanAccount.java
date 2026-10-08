package com.example.loanapplication.entity.EmiPaymentEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "LoanAccounts")
@Data
public class LoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LoanAccountId")
    private Integer loanAccountId;

    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "DealId")
    private Integer dealId;

    @Column(name = "LoanAccountNo")
    private String loanAccountNo;

    @Column(name = "LoanAmount")
    private BigDecimal loanAmount;

    @Column(name = "OutstandingPrincipal")
    private BigDecimal outstandingPrincipal;

    @Column(name = "LoanStatus")
    private String loanStatus;

    @Column(name = "InterestRate")
    private BigDecimal interestRate;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(name = "EmiAmount")
    private BigDecimal emiAmount;

    @Column(name = "DisbursementDate")
    private LocalDateTime disbursementDate;

    @Column(name = "TotalPaidAmount")
    private BigDecimal totalPaidAmount;
}