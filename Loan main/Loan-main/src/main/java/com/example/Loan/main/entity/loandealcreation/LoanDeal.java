package com.example.Loan.main.entity.loandealcreation;

import com.example.Loan.main.enums.LoanApplicationStatus;
import com.example.Loan.main.enums.loandealcreation.LoanType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "LoanDeals")
@Getter
@Setter
@NoArgsConstructor
public class LoanDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DealId")
    private Integer dealId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "ScoreCardId")
    private Integer scoreCardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "LoanType")
    private LoanType loanType;

    @Column(name = "LoanAmount", precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(
            name = "EligibleAmountAtApplication",
            precision = 18,
            scale = 2
    )
    private BigDecimal eligibleAmountAtApplication;

    @Column(name = "InterestRate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(name = "EmiAmount", precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "BankName", length = 200)
    private String bankName;

    @Column(name = "BankAccountNumber", length = 50)
    private String bankAccountNumber;

    @Column(name = "IFSCCode", length = 11)
    private String ifscCode;

    @Column(name = "EmiDay")
    private Integer emiDay;

    @Column(name = "ApprovedAmount", precision = 18, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "RequestedDisbursementDate")
    private LocalDate requestedDisbursementDate;

    @Column(name = "PropertyAddress", length = 1000)
    private String propertyAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private LoanApplicationStatus status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}