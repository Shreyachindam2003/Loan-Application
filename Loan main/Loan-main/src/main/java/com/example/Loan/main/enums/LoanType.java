package com.example.Loan.main.enums;

import java.math.BigDecimal;

public enum LoanType {

    CAR("10.00"),
    HOME("15.00");

    private final BigDecimal annualRate;

    LoanType(String annualRate) {
        this.annualRate = new BigDecimal(annualRate);
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }
}