package com.example.loanapplication.respository.EmiPaymentRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.loanapplication.entity.EmiPaymentEntity.LoanDeal;

@Repository
public interface LoanDealRepository
        extends JpaRepository<LoanDeal, Integer> {
}