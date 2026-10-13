package com.example.Loan.main.respository.loandealcreation;

import com.example.Loan.main.entity.loandealcreation.SanctionLetter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SanctionLetterRepository
        extends JpaRepository<SanctionLetter, Integer> {

    Optional<SanctionLetter> findByDealId(Integer dealId);
}