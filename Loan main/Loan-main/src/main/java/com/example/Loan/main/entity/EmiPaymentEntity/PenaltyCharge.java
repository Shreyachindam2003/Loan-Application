package com.example.Loan.main.entity.EmiPaymentEntity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "PenaltyCharges",
        indexes = {
                @Index(
                        name = "IX_PenaltyCharges_EmiScheduleId",
                        columnList = "EmiScheduleId"
                ),
                @Index(
                        name = "IX_PenaltyCharges_LoanAccountId",
                        columnList = "LoanAccountId"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UX_PenaltyCharge_Date",
                        columnNames = {
                                "EmiScheduleId",
                                "ChargeType",
                                "ChargeDate"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PenaltyChargeId")
    private Integer penaltyChargeId;

    @Column(name = "EmiScheduleId", nullable = false)
    private Integer emiScheduleId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "PenaltyAmount", precision = 18, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(name = "Reason", length = 1000)
    private String reason;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "ChargeType", nullable = false, length = 30)
    private String chargeType;

    @Column(name = "ChargeDate")
    private LocalDate chargeDate;

    @Column(name = "DaysLate", nullable = false)
    private Integer daysLate;

    @Column(name = "PaymentId")
    private Integer paymentId;
}
