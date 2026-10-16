
package com.example.Loan.main.respository;

import java.util.List;
import java.util.Optional;

import com.example.Loan.main.entity.LoanClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface LoanClosureRepository
        extends JpaRepository<LoanClosure, Integer> {

    List<LoanClosure> findByLoanAccountId(
            Integer loanAccountId
    );

    Optional<LoanClosure>
    findFirstByLoanAccountIdAndClosureStatusOrderByClosureDateDesc(
            Integer loanAccountId,
            String closureStatus
    );
}
