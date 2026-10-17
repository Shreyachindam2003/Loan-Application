
package com.example.Loan.main.respository;


import com.example.Loan.main.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanPaymentRepository
        extends JpaRepository<LoanPayment, Integer> {

    List<LoanPayment> findByLoanAccountId(Integer loanAccountId);
}
