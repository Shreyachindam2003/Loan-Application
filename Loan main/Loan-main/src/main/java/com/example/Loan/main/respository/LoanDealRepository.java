package com.example.Loan.main.respository;


import com.example.Loan.main.entity.LoanDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LoanDealRepository
        extends JpaRepository<LoanDeal, Integer> {
}