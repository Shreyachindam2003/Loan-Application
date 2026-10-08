package com.example.loanapplication.entity.closure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "LoanClosures")
@Data
public class LoanClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClosureId")
    private Integer closureId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "ClosureType", nullable = false)
    private String closureType;

    @Column(name = "FinalSettlementAmount", nullable = false)
    private BigDecimal finalSettlementAmount;

    @Column(name = "ClosureDate")
    private LocalDateTime closureDate;

    @Column(name = "ClosedBy")
    private Integer closedBy;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "ClosureStatus", nullable = false)
    private String closureStatus;
}