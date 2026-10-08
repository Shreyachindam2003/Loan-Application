package com.example.Loan.main.entity.cibilEntity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "eligibilityresults")
@Data
public class EligibilityResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EligibilityId")
    private Long  EligibilityId;

    @OneToOne
    @JoinColumn(name = "CustomerId", nullable = false, unique = true)
    private Customer customer;

    @Column(name = "CibilScore")
    private Integer CibilScore;

    @Column(name = "Status")
    private String status;

    @Column(name = "IsEligible")
    private Boolean IsEligible;

    @Column(name = "LoanAmount")
    private BigDecimal LoanAmount;

    @Column(name = "RejectionReason")
    private String RejectionReason;
}