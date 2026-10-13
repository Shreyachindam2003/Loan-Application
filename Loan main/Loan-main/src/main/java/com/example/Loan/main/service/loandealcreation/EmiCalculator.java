package com.example.Loan.main.service.loandealcreation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("loanDealEmiCalculator")
public class EmiCalculator {

    public BigDecimal calculate(
            BigDecimal principal,
            BigDecimal annualRate,
            int tenureMonths
    ) {
        if (principal == null || principal.signum() <= 0
                || annualRate == null || annualRate.signum() < 0
                || tenureMonths <= 0) {

            throw new IllegalArgumentException(
                    "Invalid EMI calculation inputs"
            );
        }

        if (annualRate.signum() == 0) {
            return principal.divide(
                    BigDecimal.valueOf(tenureMonths),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        double monthlyRate = annualRate.doubleValue() / 1200;
        double loanAmount = principal.doubleValue();

        double power = Math.pow(
                1 + monthlyRate,
                tenureMonths
        );

        double emi = (loanAmount * monthlyRate * power)
                / (power - 1);

        if (!Double.isFinite(emi) || emi <= 0) {
            throw new IllegalArgumentException(
                    "EMI could not be calculated for these inputs"
            );
        }

        return BigDecimal.valueOf(emi)
                .setScale(2, RoundingMode.HALF_UP);
    }
}//dada