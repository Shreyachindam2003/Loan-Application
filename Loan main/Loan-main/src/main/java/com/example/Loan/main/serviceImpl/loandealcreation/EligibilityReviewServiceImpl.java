
package com.example.Loan.main.serviceImpl.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.EligibilityDecisionRequest;
import com.example.Loan.main.dto.loandealcreation.response.EligibilityReviewResponse;
import com.example.Loan.main.entity.loandealcreation.EligibilityResult;
import com.example.Loan.main.exception.loandealcreation.BusinessException;
import com.example.Loan.main.exception.loandealcreation.ResourceNotFoundException;
import com.example.Loan.main.respository.loandealcreation.EligibilityResultRepository;
import com.example.Loan.main.service.loandealcreation.EligibilityReviewService;
import com.example.Loan.main.service.loandealcreation.ScoreCardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EligibilityReviewServiceImpl
        implements EligibilityReviewService {

    private final EligibilityResultRepository repository;
    private final ScoreCardService scoreCardService;

    @Override
    @Transactional(readOnly = true)
    public List<EligibilityReviewResponse> getPendingReviews() {

        return repository
                .findByReviewStatusOrderByEligibilityIdAsc("PENDING_REVIEW")
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EligibilityReviewResponse review(
            Integer eligibilityId,
            Integer officerId,
            EligibilityDecisionRequest request
    ) {

        if (eligibilityId == null || eligibilityId <= 0) {
            throw new BusinessException("A valid eligibility ID is required");
        }

        if (officerId == null || officerId <= 0) {
            throw new BusinessException("A valid officer ID is required");
        }

        if (request == null || request.decision() == null
                || request.decision().isBlank()) {
            throw new BusinessException("Decision is required");
        }

        EligibilityResult result = repository.findById(eligibilityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Eligibility result not found"
                ));

        if (!"PENDING_REVIEW".equalsIgnoreCase(result.getReviewStatus())) {
            throw new BusinessException(
                    "Only pending eligibility results can be reviewed"
            );
        }

        String decision = request.decision()
                .trim()
                .toUpperCase(Locale.ROOT);

        if ("APPROVED".equals(decision)) {

            if (result.getLoanAmount() == null
                    || result.getLoanAmount().signum() <= 0
                    || result.getCibilScore() == null
                    || result.getRiskCategory() == null
                    || result.getRiskCategory().isBlank()) {
                throw new BusinessException(
                        "Eligibility amount, CIBIL score or risk category is missing"
                );
            }

            result.setIsEligible(true);
            result.setReviewStatus("APPROVED");
            result.setRejectionReason(null);

        } else if ("REJECTED".equals(decision)) {

            if (request.remarks() == null
                    || request.remarks().isBlank()) {
                throw new BusinessException(
                        "Rejection remarks are required"
                );
            }

            result.setIsEligible(false);
            result.setReviewStatus("REJECTED");
            result.setRejectionReason(request.remarks().trim());

        } else {
            throw new BusinessException(
                    "Decision must be APPROVED or REJECTED"
            );
        }

        result.setReviewedBy(officerId);
        result.setReviewedAt(LocalDateTime.now());

        EligibilityResult saved = repository.saveAndFlush(result);

        // Generate a scorecard only after an eligibility approval.
        // Existing scorecard for this eligibility is reused by the service.
        if ("APPROVED".equals(decision)) {
            scoreCardService.generate(saved.getCustomerId());
        }

        return toResponse(saved);
    }

    private EligibilityReviewResponse toResponse(
            EligibilityResult result
    ) {
        return new EligibilityReviewResponse(
                result.getEligibilityId(),
                result.getCustomerId(),
                result.getCibilScore(),
                result.getIsEligible(),
                result.getLoanAmount(),
                result.getRiskCategory(),
                result.getReviewStatus(),
                result.getRejectionReason(),
                result.getReviewedBy(),
                result.getReviewedAt()
        );
    }
}
