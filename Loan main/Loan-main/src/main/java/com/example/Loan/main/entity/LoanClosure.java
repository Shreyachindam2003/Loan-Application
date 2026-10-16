package com.example.Loan.main.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LoanClosures")
@Getter
@Setter
public class LoanClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClosureId")
    private Integer closureId;

    @Column(name = "LoanAccountId")
    private Integer loanAccountId;

    @Column(name = "ClosureType")
    private String closureType;

    @Column(name = "FinalSettlementAmount")
    private BigDecimal finalSettlementAmount;

    @Column(name = "ClosureDate")
    private LocalDateTime closureDate;

    @Column(name = "ClosedBy")
    private Integer closedBy;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "ClosureStatus")
    private String closureStatus;

    @Column(name = "IsProcessed")
    private Boolean isProcessed;
}