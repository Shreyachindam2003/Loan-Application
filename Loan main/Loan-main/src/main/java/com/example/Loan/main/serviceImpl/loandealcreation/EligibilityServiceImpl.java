
package com.example.Loan.main.serviceImpl.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.EligibilityResponse;
import com.example.Loan.main.exception.loandealcreation.ResourceNotFoundException;
import com.example.Loan.main.respository.loandealcreation.EligibilityView;
import com.example.Loan.main.respository.loandealcreation.ScoreCardRepository;
import com.example.Loan.main.service.loandealcreation.EligibilityService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("loanDealEligibilityService")
@RequiredArgsConstructor
public class EligibilityServiceImpl implements EligibilityService {

    private final ScoreCardRepository repo;

    @Override
    @Transactional(readOnly = true)
    public EligibilityResponse getLatestByCustomerId(Integer customerId) {

        EligibilityView result = repo.findLatestEligibility(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Eligibility result not found"
                ));

        return new EligibilityResponse(
                result.getEligibilityId(),
                result.getCustomerId(),
                result.getCibilScore(),
                result.getEligible(),
                result.getLoanAmount(),
                result.getRiskCategory(),
                result.getReviewStatus()
        );
    }
}

    