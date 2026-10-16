package com.example.Loan.main.entity.loandealcreation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "EligibilityResults")
@Getter
@Setter
@NoArgsConstructor
public class EligibilityResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EligibilityId")
    private Integer eligibilityId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "CibilScore", nullable = false)
    private Integer cibilScore;

    @Column(name = "IsEligible", nullable = false)
    private Boolean isEligible;

    @Column(name = "LoanAmount", nullable = false,
            precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "RiskCategory")
    private String riskCategory;

    @Column(name = "ReviewStatus")
    private String reviewStatus;

    @Column(name = "RejectionReason")
    private String rejectionReason;

    @Column(name = "ReviewedBy")
    private Integer reviewedBy;

    @Column(name = "ReviewedAt")
    private LocalDateTime reviewedAt;
}