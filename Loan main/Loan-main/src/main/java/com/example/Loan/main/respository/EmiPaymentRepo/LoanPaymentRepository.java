package com.example.Loan.main.respository.EmiPaymentRepo;


import java.util.List;
import java.util.Optional;

import com.example.Loan.main.entity.EmiPaymentEntity.LoanPayment;
import com.example.Loan.main.enums.EmiPaymentEnums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LoanPaymentRepository
        extends JpaRepository<LoanPayment, Integer> {

    List<LoanPayment> findByLoanAccountIdAndPaymentStatus(
            Integer loanAccountId,
            PaymentStatus paymentStatus);

    List<LoanPayment> findByLoanAccountIdOrderByPaymentDateDesc(
            Integer loanAccountId);

    Optional<LoanPayment> findByIdempotencyKey(
            String idempotencyKey);

    Optional<LoanPayment> findByRazorpayOrderId(
            String razorpayOrderId);

    Optional<LoanPayment> findByRazorpayPaymentId(
            String razorpayPaymentId);

    Optional<LoanPayment> findByEmiScheduleId(
            Integer emiScheduleId);
    // Find payment linked to a specific foreclosure request
    Optional<LoanPayment> findByForeclosureRequestIdAndPaymentPurpose(
            Integer foreclosureRequestId,
            String paymentPurpose);
}

