package com.example.Loan.main.respository.loandealcreation;

import com.example.Loan.main.entity.loandealcreation.ScoreCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScoreCardRepository
        extends JpaRepository<ScoreCard, Integer> {

    Optional<ScoreCard> findByEligibilityId(
            Integer eligibilityId
    );

    Optional<ScoreCard> findFirstByCustomerIdOrderByAppliedDateDescScoreCardIdDesc(
            Integer customerId
    );

    @Query(
            value = """
                    SELECT
                        EligibilityId AS eligibilityId,
                        CustomerId AS customerId,
                        CibilScore AS cibilScore,
                        IsEligible AS eligible,
                        LoanAmount AS loanAmount,
                        RiskCategory AS riskCategory,
                        ReviewStatus AS reviewStatus
                    FROM EligibilityResults
                    WHERE CustomerId = :customerId
                    ORDER BY EligibilityId DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<EligibilityView> findLatestEligibility(
            @Param("customerId") Integer customerId
    );
}