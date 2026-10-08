package com.example.loanapplication.repository.closure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.loanapplication.entity.closure.LoanClosure;

@Repository
public interface LoanClosureRepository
        extends JpaRepository<LoanClosure, Integer> {

    List<LoanClosure> findByLoanAccountId(Integer loanAccountId);

    Optional<LoanClosure> findFirstByLoanAccountIdAndClosureStatusOrderByClosureDateDesc(
            Integer loanAccountId,
            String closureStatus);
}