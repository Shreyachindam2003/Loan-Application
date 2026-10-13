package com.example.Loan.main.entity.loandealcreation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ScoreCards")
@Getter
@Setter
@NoArgsConstructor
public class ScoreCard {
    //dada
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScoreCardId")
    private Integer scoreCardId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "EligibilityId", unique = true)
    private Integer eligibilityId;

    @Column(name = "CurrentStatus")
    private String currentStatus;

    @Column(name = "AppliedDate")
    private LocalDateTime appliedDate;

    @Column(name = "CibilScore")
    private Integer cibilScore;

    @Column(name = "RiskCategory")
    private String riskCategory;

    @Column(
            name = "EligibleLoanAmount",
            precision = 18,
            scale = 2
    )
    private BigDecimal eligibleLoanAmount;
}