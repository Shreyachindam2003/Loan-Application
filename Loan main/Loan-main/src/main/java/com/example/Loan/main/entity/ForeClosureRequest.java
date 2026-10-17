
package com.example.Loan.main.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ForeClosureRequests")
public class ForeClosureRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestId")
    private Integer requestId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "ForeClosureType", nullable = false)
    private String foreClosureType;

    @Column(name = "ForeClosureAmount", precision = 18, scale = 2)
    private BigDecimal foreClosureAmount;

    @Column(name = "PartialAmount", precision = 18, scale = 2)
    private BigDecimal partialAmount;

    @Column(name = "PartialTenureMonths")
    private Integer partialTenureMonths;

    @Column(name = "RevisedEmiAmount", precision = 18, scale = 2)
    private BigDecimal revisedEmiAmount;

    @Column(name = "RemainingPrincipal", precision = 18, scale = 2)
    private BigDecimal remainingPrincipal;

    @Column(name = "RequestedDate")
    private LocalDateTime requestedDate;

    @Column(name = "ExpectedClosureDate")
    private LocalDateTime expectedClosureDate;

    @Column(name = "Reason", length = 1000)
    private String reason;

    @Column(name = "Status")
    private String status;

    @Column(name = "IsPaid")
    private Boolean isPaid;

    @Column(name = "PaidDate")
    private LocalDateTime paidDate;

    @Column(name = "ClosedBy")
    private Integer closedBy;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

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

    public BigDecimal getForeClosureAmount() {
        return foreClosureAmount;
    }

    public void setForeClosureAmount(BigDecimal foreClosureAmount) {
        this.foreClosureAmount = foreClosureAmount;
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

    public BigDecimal getRevisedEmiAmount() {
        return revisedEmiAmount;
    }

    public void setRevisedEmiAmount(BigDecimal revisedEmiAmount) {
        this.revisedEmiAmount = revisedEmiAmount;
    }

    public BigDecimal getRemainingPrincipal() {
        return remainingPrincipal;
    }

    public void setRemainingPrincipal(BigDecimal remainingPrincipal) {
        this.remainingPrincipal = remainingPrincipal;
    }

    public LocalDateTime getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
    }

    public LocalDateTime getExpectedClosureDate() {
        return expectedClosureDate;
    }

    public void setExpectedClosureDate(LocalDateTime expectedClosureDate) {
        this.expectedClosureDate = expectedClosureDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }

    public Integer getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(Integer closedBy) {
        this.closedBy = closedBy;
    }
}
