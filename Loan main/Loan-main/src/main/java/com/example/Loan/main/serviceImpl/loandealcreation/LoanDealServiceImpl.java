
package com.example.Loan.main.serviceImpl.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.LoanApplyRequest;
import com.example.Loan.main.dto.loandealcreation.response.LoanDealResponse;
import com.example.Loan.main.dto.loandealcreation.response.PageResponse;
import com.example.Loan.main.entity.loandealcreation.LoanDeal;
import com.example.Loan.main.entity.loandealcreation.ScoreCard;

import com.example.Loan.main.enums.loandealcreation.LoanApplicationStatus;
import com.example.Loan.main.enums.loandealcreation.LoanType;
import com.example.Loan.main.exception.loandealcreation.BusinessException;
import com.example.Loan.main.exception.loandealcreation.ResourceNotFoundException;
import com.example.Loan.main.respository.loandealcreation.EligibilityView;
import com.example.Loan.main.respository.loandealcreation.LoanDealRepository;
import com.example.Loan.main.respository.loandealcreation.ScoreCardRepository;
import com.example.Loan.main.service.loandealcreation.EmiCalculator;
import com.example.Loan.main.service.loandealcreation.LoanDealService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LoanDealServiceImpl implements LoanDealService {

    private final LoanDealRepository repo;
    private final ScoreCardRepository cardRepo;
    private final EmiCalculator emiCalculator;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public LoanDealResponse apply(
            Integer customerId,
            LoanApplyRequest request
    ) {

        ScoreCard card = cardRepo.findById(request.scoreCardId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Scorecard not found"
                ));

        if (!Objects.equals(card.getCustomerId(), customerId)) {
            throw new BusinessException(
                    "Scorecard does not belong to this customer"
            );
        }

        EligibilityView eligibility =
                cardRepo.findLatestEligibility(customerId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Eligibility result not found"
                        ));

        if (!Boolean.TRUE.equals(eligibility.getEligible())
                || !Objects.equals(
                card.getEligibilityId(),
                eligibility.getEligibilityId()
        )) {

            throw new BusinessException(
                    "Generate a scorecard from the latest approved eligibility"
            );
        }

        if (!"GENERATED".equals(card.getCurrentStatus())
                || card.getEligibleLoanAmount() == null
                || card.getEligibleLoanAmount().signum() <= 0) {

            throw new BusinessException("Scorecard is not valid");
        }

        if (eligibility.getLoanAmount() == null
                || eligibility.getLoanAmount().compareTo(
                card.getEligibleLoanAmount()
        ) != 0) {

            throw new BusinessException(
                    "Eligibility amount changed; regenerate the scorecard"
            );
        }

        if (request.loanAmount().compareTo(
                card.getEligibleLoanAmount()
        ) > 0) {

            throw new BusinessException(
                    "Loan amount cannot exceed eligible amount: "
                            + card.getEligibleLoanAmount()
            );
        }

        if (request.loanType() == LoanType.HOME
                && (request.propertyAddress() == null
                || request.propertyAddress().isBlank())) {

            throw new BusinessException(
                    "Property address is required for a HOME loan"
            );
        }

        LoanDeal deal = modelMapper.map(
                request,
                LoanDeal.class
        );

        deal.setCustomerId(customerId);
        deal.setScoreCardId(card.getScoreCardId());
        deal.setEligibleAmountAtApplication(
                card.getEligibleLoanAmount()
        );
        deal.setInterestRate(request.loanType().getAnnualRate());

        deal.setEmiAmount(emiCalculator.calculate(
                request.loanAmount(),
                deal.getInterestRate(),
                request.tenureMonths()
        ));

        deal.setStatus(LoanApplicationStatus.PENDING_REVIEW);
        deal.setCreatedAt(LocalDateTime.now());

        return response(repo.save(deal));
    }

    @Override
    @Transactional(readOnly = true)
    public LoanDealResponse getById(Integer dealId) {

        LoanDeal deal = repo.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan application not found"
                ));

        return response(deal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDealResponse> getPendingDeals() {

        return repo.findByStatusOrderByCreatedAtAsc(
                        LoanApplicationStatus.PENDING_REVIEW
                )
                .stream()
                .map(this::response)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanDealResponse getLatestByCustomerId(Integer customerId) {

        LoanDeal deal = repo
                .findFirstByCustomerIdOrderByCreatedAtDescDealIdDesc(
                        customerId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer loan application not found"
                ));

        return response(deal);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LoanDealResponse> getCustomerDeals(
            Integer customerId,
            int page,
            int size
    ) {

        PageRequest pageable = PaginationSupport.page(
                page,
                size,
                "createdAt",
                "dealId"
        );

        Page<LoanDealResponse> result = repo
                .findByCustomerId(customerId, pageable)
                .map(this::response);

        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LoanDealResponse> getDealsByStatus(
            LoanApplicationStatus status,
            int page,
            int size
    ) {

        if (status == null) {
            throw new BusinessException(
                    "Loan application status is required"
            );
        }

        PageRequest pageable = PaginationSupport.page(
                page,
                size,
                "createdAt",
                "dealId"
        );

        Page<LoanDealResponse> result = repo
                .findByStatus(status, pageable)
                .map(this::response);

        return PageResponse.from(result);
    }


    private LoanDealResponse response(LoanDeal deal) {

        return new LoanDealResponse(
                deal.getDealId(),
                deal.getCustomerId(),
                deal.getScoreCardId(),
                deal.getLoanType(),
                deal.getLoanAmount(),
                deal.getEligibleAmountAtApplication(),
                deal.getInterestRate(),
                deal.getTenureMonths(),
                deal.getEmiAmount(),
                deal.getRequestedDisbursementDate(),
                deal.getEmiDay(),
                deal.getStatus(),
                deal.getCreatedAt()
        );
    }


}
