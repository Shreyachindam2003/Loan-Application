package com.example.Loan.main.entity;


import java.math.BigDecimal;
import java.time.LocalDate;


import com.example.Loan.main.enums.ChargeType;
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
@Table(name = "PenaltyCharges")
@Getter
@Setter
public class PenaltyCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PenaltyChargeId")
    private Integer penaltyChargeId;

    @Column(name = "LoanAccountId")
    private Integer loanAccountId;

    @Column(name = "EmiScheduleId")
    private Integer emiScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ChargeType")
    private ChargeType chargeType;

    @Column(name = "Amount")
    private BigDecimal amount;

    @Column(name = "ChargeDate")
    private LocalDate chargeDate;

    @Column(name = "DaysLate")
    private Integer daysLate;

    @Column(name = "PaymentId")
    private Integer paymentId;
}