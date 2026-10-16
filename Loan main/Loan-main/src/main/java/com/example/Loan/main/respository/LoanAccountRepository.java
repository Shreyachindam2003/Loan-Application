package com.example.Loan.main.respository;


import com.example.Loan.main.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanAccountRepository
        extends JpaRepository<LoanAccount, Integer> {

    Optional<LoanAccount> findByDealId(Integer dealId);

    List<LoanAccount> findByCustomerIdOrderByCreatedAtDesc(
            Integer customerId
    );
}