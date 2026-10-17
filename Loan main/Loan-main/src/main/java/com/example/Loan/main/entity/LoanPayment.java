
package com.example.Loan.main.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LoanPayments")
public class LoanPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentId")
    private Integer paymentId;

    @Column(name = "LoanAccountId", nullable = false)
    private Integer loanAccountId;

    @Column(name = "PaymentAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @Column(name = "PaymentStatus", nullable = false)
    private String paymentStatus;

    @Column(name = "PaymentName")
    private String paymentName;

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getLoanAccountId() {
        return loanAccountId;
    }

    public void setLoanAccountId(Integer loanAccountId) {
        this.loanAccountId = loanAccountId;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentName() {
        return paymentName;
    }

    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }
}
