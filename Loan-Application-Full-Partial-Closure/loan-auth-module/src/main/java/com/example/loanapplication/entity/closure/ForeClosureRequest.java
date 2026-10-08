package com.example.loanapplication.entity.closure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ForeClosureRequests")
@Data
public class ForeClosureRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestId")
    private Integer requestId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "ForeClosureType", nullable = false)
    private String foreClosureType;

    @Column(name = "ForeClosureAmount")
    private BigDecimal foreClosureAmount;

    @Column(name = "PartialAmount")
    private BigDecimal partialAmount;

    @Column(name = "PartialTenureMonths")
    private Integer partialTenureMonths;

    @Column(name = "RevisedEmiAmount")
    private BigDecimal revisedEmiAmount;

    @Column(name = "RemainingPrincipal")
    private BigDecimal remainingPrincipal;

    @Column(name = "RequestedDate")
    private LocalDateTime requestedDate;

    @Column(name = "ExpectedClosureDate")
    private LocalDateTime expectedClosureDate;

    @Column(name = "Reason")
    private String reason;

    @Column(name = "Status")
    private String status;

    @Column(name = "IsPaid")
    private Boolean isPaid;

    @Column(name = "PaidDate")
    private LocalDateTime paidDate;

    @Column(name = "ClosedBy")
    private Integer closedBy;
}