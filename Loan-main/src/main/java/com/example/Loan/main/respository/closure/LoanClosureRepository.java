package com.example.Loan.main.respository.closure;

import com.example.Loan.main.entity.closure.LoanClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface LoanClosureRepository
        extends JpaRepository<LoanClosure, Integer> {

    List<LoanClosure> findByLoanAccountId(Integer loanAccountId);

    Optional<LoanClosure> findFirstByLoanAccountIdAndClosureStatusOrderByClosureDateDesc(
            Integer loanAccountId,
            String closureStatus);
}