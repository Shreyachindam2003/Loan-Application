package com.example.Loan.main.entity;


import com.example.Loan.main.enums.LoanType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "LoanDeals")
@Getter
@Setter
@NoArgsConstructor
public class LoanDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DealId")
    private Integer dealId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "ScoreCardId")
    private Integer scoreCardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "LoanType")
    private LoanType loanType;

    @Column(name = "LoanAmount", precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "EligibleAmountAtApplication", precision = 18, scale = 2)
    private BigDecimal eligibleAmountAtApplication;

    @Column(name = "InterestRate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(name = "EmiAmount", precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "BankName")
    private String bankName;

    @Column(name = "BankAccountNumber")
    private String bankAccountNumber;

    @Column(name = "IFSCCode")
    private String ifscCode;

    @Column(name = "EmiDay")
    private Integer emiDay;

    @Column(name = "ApprovedAmount", precision = 18, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "RequestedDisbursementDate")
    private LocalDate requestedDisbursementDate;

    @Column(name = "Status")
    private String status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}