package com.example.Loan.main.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;


import com.example.Loan.main.enums.PaymentStatus;
import com.example.Loan.main.enums.PaymentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "LoanPayments")
@Getter
@Setter
public class LoanPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentId")
    private Integer paymentId;

    @Column(name = "LoanAccountId")
    private Integer loanAccountId;

    @Column(name = "EmiScheduleId")
    private Integer emiScheduleId;

    @Column(name = "Amount")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentStatus")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "PaymentType")
    private PaymentType paymentType;

    @Column(name = "IdempotencyKey")
    private String idempotencyKey;

    @Column(name = "RazorpayOrderId")
    private String razorpayOrderId;

    @Column(name = "RazorpayPaymentId")
    private String razorpayPaymentId;

    @Column(name = "RazorpaySignature")
    private String razorpaySignature;

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;
}
