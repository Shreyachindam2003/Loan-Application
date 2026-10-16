package com.example.Loan.main.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Loan.main.entity.LoanClosure;

public interface LoanClosureRepository
        extends JpaRepository<LoanClosure, Integer> {

    Optional<LoanClosure>
    findFirstByLoanAccountIdAndClosureStatusOrderByClosureDateDesc(
            Integer loanAccountId,
            String closureStatus);

    Optional<LoanClosure>
    findFirstByLoanAccountIdAndClosureStatusAndIsProcessedFalseOrderByClosureDateAsc(
            Integer loanAccountId, String closureStatus);
}