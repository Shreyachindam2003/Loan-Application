package com.example.Loan.main.respository.loandealcreation;

import com.example.Loan.main.entity.loandealcreation.LoanAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanAccountRepository
        extends JpaRepository<LoanAccount, Integer> {

    Optional<LoanAccount> findByDealId(
            Integer dealId
    );

    List<LoanAccount> findByCustomerIdOrderByCreatedAtDesc(
            Integer customerId
    );

    Optional<LoanAccount> findFirstByCustomerIdOrderByCreatedAtDescLoanAccountIdDesc(
            Integer customerId
    );

    Page<LoanAccount> findByCustomerId(
            Integer customerId,
            Pageable pageable
    );
}