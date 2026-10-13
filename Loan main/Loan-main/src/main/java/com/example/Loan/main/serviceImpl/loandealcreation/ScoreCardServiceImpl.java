package com.example.Loan.main.serviceImpl.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.ScoreCardResponse;
import com.example.Loan.main.entity.loandealcreation.ScoreCard;
import com.example.Loan.main.exception.loandealcreation.BusinessException;
import com.example.Loan.main.exception.loandealcreation.ResourceNotFoundException;
import com.example.Loan.main.repository.loandealcreation.EligibilityView;
import com.example.Loan.main.respository.loandealcreation.ScoreCardRepository;
import com.example.Loan.main.service.loandealcreation.ScoreCardService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScoreCardServiceImpl implements ScoreCardService {

    private final ScoreCardRepository repo;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ScoreCardResponse generate(Integer customerId) {
        EligibilityView result = repo.findLatestEligibility(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Eligibility result not found"
                ));

        if (!Boolean.TRUE.equals(result.getEligible())) {
            throw new BusinessException(
                    "Scorecard requires approved eligibility"
            );
        }

        if (result.getLoanAmount() == null
                || result.getLoanAmount().signum() <= 0
                || result.getCibilScore() == null
                || result.getRiskCategory() == null
                || result.getRiskCategory().isBlank()) {

            throw new BusinessException(
                    "Eligibility amount, CIBIL score or risk category is missing"
            );
        }

        ScoreCard card = repo.findByEligibilityId(
                result.getEligibilityId()
        ).orElse(null);

        if (card == null) {
            card = new ScoreCard();
            card.setCustomerId(result.getCustomerId());
            card.setEligibilityId(result.getEligibilityId());
            card.setCibilScore(result.getCibilScore());
            card.setRiskCategory(result.getRiskCategory());
            card.setEligibleLoanAmount(result.getLoanAmount());
            card.setCurrentStatus("GENERATED");
            card.setAppliedDate(LocalDateTime.now());

            card = repo.save(card);
        }

        return modelMapper.map(card, ScoreCardResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ScoreCardResponse getById(Integer scoreCardId) {
        ScoreCard card = repo.findById(scoreCardId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Scorecard not found"
                ));

        return modelMapper.map(card, ScoreCardResponse.class);
    }
}//dada