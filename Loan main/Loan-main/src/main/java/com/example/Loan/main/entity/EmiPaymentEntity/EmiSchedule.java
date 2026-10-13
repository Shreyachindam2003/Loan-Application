package com.example.Loan.main.entity.EmiPaymentEntity;



import java.math.BigDecimal;
import java.time.LocalDate;


import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
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
@Table(name = "EmiSchedules")
@Getter
@Setter
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EmiScheduleId")
    private Integer emiScheduleId;

    @Column(name = "LoanAccountId")
    private Integer loanAccountId;

    @Column(name = "InstallmentNo")
    private Integer installmentNo;

    @Column(name = "DueDate")
    private LocalDate dueDate;

    @Column(name = "OpeningBalance")
    private BigDecimal openingBalance;

    @Column(name = "EmiAmount")
    private BigDecimal emiAmount;

    @Column(name = "PrincipalAmount")
    private BigDecimal principalAmount;

    @Column(name = "InterestAmount")
    private BigDecimal interestAmount;

    @Column(name = "ClosingBalance")
    private BigDecimal closingBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private EmiStatus status;
}
