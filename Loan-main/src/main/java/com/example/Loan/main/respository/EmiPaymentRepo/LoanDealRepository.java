package com.example.Loan.main.respository.EmiPaymentRepo;

import com.example.Loan.main.entity.EmiPaymentEntity.LoanDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LoanDealRepository
        extends JpaRepository<LoanDeal, Integer> {
}