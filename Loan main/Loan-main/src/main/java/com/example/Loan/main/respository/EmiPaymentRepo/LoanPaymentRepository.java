package com.example.Loan.main.respository.EmiPaymentRepo;


import com.example.Loan.main.entity.EmiPaymentEntity.LoanPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Integer> {

    Optional<LoanPayment> findByIdempotencyKey(
            String idempotencyKey
    );

    Optional<LoanPayment> findByRazorpayOrderId(
            String razorpayOrderId
    );

    Optional<LoanPayment> findByRazorpayPaymentId(
            String razorpayPaymentId
    );

    Page<LoanPayment> findByLoanAccountIdOrderByPaymentIdDesc(
            Integer loanAccountId,
            Pageable pageable
    );

    @Query("""
        SELECT p
        FROM LoanPayment p
        WHERE p.loanAccountId = :loanAccountId
        AND p.paymentStatus = :status
        ORDER BY p.paymentId DESC
    """)
    Page<LoanPayment> findByLoanAccountIdAndStatus(
            @Param("loanAccountId") Integer loanAccountId,
            @Param("status") String status,
            Pageable pageable
    );
}
