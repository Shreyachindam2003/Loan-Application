package com.example.Loan.main.entity.loandealcreation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SanctionLetters")
@Getter
@Setter
@NoArgsConstructor
public class SanctionLetter {
    //dada
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SanctionId")
    private Integer sanctionId;

    @Column(name = "DealId", nullable = false, unique = true)
    private Integer dealId;

    @Column(
            name = "LoanAmount",
            precision = 18,
            scale = 2
    )
    private BigDecimal loanAmount;

    @Column(
            name = "InterestRate",
            precision = 8,
            scale = 4
    )
    private BigDecimal interestRate;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(
            name = "EmiAmount",
            precision = 18,
            scale = 2
    )
    private BigDecimal emiAmount;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "ApplicantName", length = 201)
    private String applicantName;

    @Column(name = "MobileNo", length = 20)
    private String mobileNo;

    @Column(name = "LoanType")
    private String loanType;

    @Column(name = "PropertyAddress", length = 1000)
    private String propertyAddress;

    @Column(
            name = "ProcessingChargePercent",
            precision = 8,
            scale = 4
    )
    private BigDecimal processingChargePercent;

    @Column(
            name = "ProcessingChargeAmount",
            precision = 18,
            scale = 2
    )
    private BigDecimal processingChargeAmount;

    @Column(
            name = "OriginationFee",
            precision = 18,
            scale = 2
    )
    private BigDecimal originationFee;

    @Column(name = "ValidityDays")
    private Integer validityDays;

    @Column(name = "IssuingBank")
    private String issuingBank;

    @Column(name = "AdditionalConditions", columnDefinition = "TEXT")
    private String additionalConditions;

    @Column(name = "AuthorizedSignatory")
    private String authorizedSignatory;
}