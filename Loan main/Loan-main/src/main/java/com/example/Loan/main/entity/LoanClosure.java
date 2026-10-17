
package com.example.Loan.main.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "IsProcessed")
    private Boolean isProcessed;
}
