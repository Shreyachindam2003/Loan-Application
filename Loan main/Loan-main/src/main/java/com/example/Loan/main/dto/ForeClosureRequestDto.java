
package com.example.Loan.main.dto;

import java.math.BigDecimal;

public class ForeClosureRequestDto {

    private Integer loanAccountId;
    private String foreClosureType;
    private BigDecimal partialAmount;
    private Integer partialTenureMonths;
    private String reason;

    public Integer getLoanAccountId() {
        return loanAccountId;
    }

    public void setLoanAccountId(Integer loanAccountId) {
        this.loanAccountId = loanAccountId;
    }

    public String getForeClosureType() {
        return foreClosureType;
    }

    public void setForeClosureType(String foreClosureType) {
        this.foreClosureType = foreClosureType;
    }

    public BigDecimal getPartialAmount() {
        return partialAmount;
    }

    public void setPartialAmount(BigDecimal partialAmount) {
        this.partialAmount = partialAmount;
    }

    public Integer getPartialTenureMonths() {
        return partialTenureMonths;
    }

    public void setPartialTenureMonths(Integer partialTenureMonths) {
        this.partialTenureMonths = partialTenureMonths;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
