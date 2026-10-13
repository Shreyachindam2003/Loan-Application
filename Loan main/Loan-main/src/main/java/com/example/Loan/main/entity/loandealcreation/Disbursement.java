package com.example.Loan.main.entity.loandealcreation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Disbursements")
@Getter
@Setter
@NoArgsConstructor
public class Disbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DisbursementId")
    private Integer disbursementId;

    @Column(name = "DealId", nullable = false, unique = true)
    private Integer dealId;

    @Column(
            name = "DisburseAmount",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal disburseAmount;

    @Column(name = "BankPartner")
    private String bankPartner;

    @Column(name = "DisbursementDate")
    private LocalDateTime disbursementDate;

    @Column(name = "Status")
    private String status;
}