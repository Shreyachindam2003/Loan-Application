package com.example.Loan.main.util.EmiPaymentUtil;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class EmiCalculator {

    public BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal annualRate,
            int tenureMonths) {

        BigDecimal monthlyRate=annualRate.divide(
                BigDecimal.valueOf(12*100),
                10,
                RoundingMode.HALF_UP
        );

        double p=principal.doubleValue();
        double r=monthlyRate.doubleValue();
        double n=tenureMonths;

        double emi=p*r*Math.pow(1+r,n)/(Math.pow(1+r,n)-1);

        return BigDecimal.valueOf(emi)
                .setScale(2,RoundingMode.HALF_UP);
    }

    public BigDecimal calculateMonthlyInterest(
            BigDecimal balance,
            BigDecimal annualRate) {

        return balance.multiply(annualRate)
                .divide(
                        BigDecimal.valueOf(12*100),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    public int calculateRemainingTenure(
            BigDecimal principal,
            BigDecimal emi,
            BigDecimal annualRate) {

        BigDecimal monthlyRate=annualRate.divide(
                BigDecimal.valueOf(12*100),
                10,
                RoundingMode.HALF_UP
        );

        double p=principal.doubleValue();
        double e=emi.doubleValue();
        double r=monthlyRate.doubleValue();

        if(r==0){
            return (int)Math.ceil(p/e);
        }

        double months=
                -Math.log(1-(p*r/e))
                        /Math.log(1+r);

        return (int)Math.ceil(months);
    }
}