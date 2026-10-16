package com.example.Loan.main.controller.loandealcreation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class EligibilityCalculator {

    public BigDecimal calculateEligibleAmount(
            BigDecimal netMonthlyIncome,
            BigDecimal allowedIncomePercentage,
            BigDecimal existingMonthlyEmi,
            BigDecimal annualInterestRate,
            int tenureMonths
    ) {

        validateInputs(
                netMonthlyIncome,
                allowedIncomePercentage,
                existingMonthlyEmi,
                annualInterestRate,
                tenureMonths
        );

        BigDecimal maximumEmi = netMonthlyIncome
                .multiply(allowedIncomePercentage)
                .divide(
                        BigDecimal.valueOf(100),
                        2,
                        RoundingMode.DOWN
                );

        BigDecimal availableEmi = maximumEmi
                .subtract(existingMonthlyEmi);

        if (availableEmi.signum() <= 0) {
            return new BigDecimal("0.00");
        }

        if (annualInterestRate.signum() == 0) {
            return availableEmi
                    .multiply(BigDecimal.valueOf(tenureMonths))
                    .setScale(2, RoundingMode.DOWN);
        }

        double monthlyRate = annualInterestRate.doubleValue()
                / 12
                / 100;

        double factor = Math.pow(
                1 + monthlyRate,
                tenureMonths
        );

        double eligibleAmount = availableEmi.doubleValue()
                * (factor - 1)
                / (monthlyRate * factor);

        if (!Double.isFinite(eligibleAmount)) {
            throw new IllegalArgumentException(
                    "Unable to calculate eligible loan amount"
            );
        }

        return BigDecimal.valueOf(eligibleAmount)
                .setScale(2, RoundingMode.DOWN);
    }

    private void validateInputs(
            BigDecimal netMonthlyIncome,
            BigDecimal allowedIncomePercentage,
            BigDecimal existingMonthlyEmi,
            BigDecimal annualInterestRate,
            int tenureMonths
    ) {

        if (netMonthlyIncome == null
                || netMonthlyIncome.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Net monthly income must be greater than zero"
            );
        }

        if (allowedIncomePercentage == null
                || allowedIncomePercentage.signum() <= 0
                || allowedIncomePercentage.compareTo(
                BigDecimal.valueOf(100)
        ) > 0) {

            throw new IllegalArgumentException(
                    "Allowed income percentage must be greater "
                            + "than zero and at most 100"
            );
        }

        if (existingMonthlyEmi == null
                || existingMonthlyEmi.signum() < 0) {

            throw new IllegalArgumentException(
                    "Existing monthly EMI cannot be negative"
            );
        }

        if (annualInterestRate == null
                || annualInterestRate.signum() < 0) {

            throw new IllegalArgumentException(
                    "Annual interest rate cannot be negative"
            );
        }

        if (tenureMonths <= 0 || tenureMonths > 360) {
            throw new IllegalArgumentException(
                    "Tenure must be between 1 and 360 months"
            );
        }
    }
}