package com.example.loanapplication.repository.closure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.loanapplication.entity.closure.LoanPayment;

@Repository
public interface LoanPaymentRepository
        extends JpaRepository<LoanPayment, Integer> {

    List<LoanPayment> findByLoanAccountId(Integer loanAccountId);
}