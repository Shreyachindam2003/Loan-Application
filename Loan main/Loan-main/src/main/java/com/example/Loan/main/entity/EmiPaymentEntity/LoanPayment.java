package com.example.Loan.main.entity.EmiPaymentEntity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "LoanPayments",
        indexes = {
                @Index(
                        name = "IX_LoanPayments_LoanAccountId",
                        columnList = "LoanAccountId"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UX_LoanPayments_IdempotencyKey",
                        columnNames = "IdempotencyKey"
                ),
                @UniqueConstraint(
                        name = "UX_LoanPayments_RazorpayOrderId",
                        columnNames = "RazorpayOrderId"
                ),
                @UniqueConstraint(
                        name = "UX_LoanPayments_RazorpayPaymentId",
                        columnNames = "RazorpayPaymentId"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentId")
    private Integer paymentId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "PaymentAmount", precision = 18, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @Column(name = "PaymentStatus", length = 50)
    private String paymentStatus;

    @Column(name = "PaymentName", length = 200)
    private String paymentName;

    @Column(name = "IdempotencyKey", length = 100)
    private String idempotencyKey;

    @Column(name = "RazorpayOrderId", length = 100)
    private String razorpayOrderId;

    @Column(name = "RazorpayPaymentId", length = 100)
    private String razorpayPaymentId;

    @Column(name = "RazorpaySignature", length = 500)
    private String razorpaySignature;

    @Column(name = "PaymentType", length = 30)
    private String paymentType;
}