package com.example.Loan.main.respository.loandealcreation;

import com.example.Loan.main.entity.loandealcreation.EligibilityResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EligibilityResultRepository
        extends JpaRepository<EligibilityResult, Integer> {

    List<EligibilityResult> findByReviewStatusOrderByEligibilityIdAsc(
            String reviewStatus
    );
}