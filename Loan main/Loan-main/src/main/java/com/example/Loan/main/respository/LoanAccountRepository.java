package com.example.Loan.main.respository;

import com.example.Loan.main.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Integer> {

    List<LoanAccount> findByCustomerId(Integer customerId);
}