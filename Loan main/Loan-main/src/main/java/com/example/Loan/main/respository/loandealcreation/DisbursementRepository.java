package com.example.Loan.main.respository.loandealcreation;

import com.example.Loan.main.entity.loandealcreation.Disbursement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisbursementRepository
        extends JpaRepository<Disbursement, Integer> {

    Optional<Disbursement> findByDealId(Integer dealId);
}