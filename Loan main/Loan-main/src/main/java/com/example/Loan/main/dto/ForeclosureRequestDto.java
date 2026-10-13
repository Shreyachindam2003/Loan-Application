package com.example.Loan.main.dto;


import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForeclosureRequestDto {

    private String closureType; // FULL or PARTIAL

    private BigDecimal amount;

    private Integer remainingTenureMonths;

    private Integer closedBy;

    private String remarks;
}