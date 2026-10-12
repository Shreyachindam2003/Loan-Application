package com.example.Loan.main.respository;

import com.example.Loan.main.entity.Disbursement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisbursementRepository
        extends JpaRepository<Disbursement, Integer> {

    Optional<Disbursement> findByDealId(Integer dealId);
}