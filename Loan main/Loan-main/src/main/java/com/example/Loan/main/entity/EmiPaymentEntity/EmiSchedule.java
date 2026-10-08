package com.example.Loan.main.entity.EmiPaymentEntity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "EmiSchedules",
        indexes = {
                @Index(
                        name = "IX_EmiSchedules_LoanAccountId",
                        columnList = "LoanAccountId"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UX_EmiSchedules_Loan_Installment",
                        columnNames = {"LoanAccountId", "InstallmentNo"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EmiScheduleId")
    private Integer emiScheduleId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "InstallmentNo", nullable = false)
    private Integer installmentNo;

    @Column(name = "DueDate", nullable = false)
    private LocalDate dueDate;

    @Column(name = "PrincipalAmount", precision = 18, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "InterestAmount", precision = 18, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "OpeningBalance", precision = 18, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "ClosingBalance", precision = 18, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "Emi", precision = 18, scale = 2)
    private BigDecimal emi;

    @Column(name = "PaymentStatus", length = 50)
    private String paymentStatus;

    @Column(name = "PaidDate")
    private LocalDateTime paidDate;

    @Column(name = "CancellationReason", length = 1000)
    private String cancellationReason;

    @Column(name = "PaymentId")
    private Integer paymentId;
}