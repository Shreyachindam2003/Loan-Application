package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyResponse;
import com.example.Loan.main.entity.EmiPaymentEntity.PenaltyCharge;
import com.example.Loan.main.exception.PenaltyNotFoundException;
import com.example.Loan.main.respository.EmiPaymentRepo.PenaltyChargeRepository;
import com.example.Loan.main.service.EmiPaymentService.PenaltyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyServiceImpl implements PenaltyService {

    private final PenaltyChargeRepository penaltyRepo;
    private final ModelMapper mapper;

    // Get all penalties and bonus charges for a loan
    @Override
    public List<PenaltyResponse> getPenalties(Integer loanAccountId) {

        if (loanAccountId == null || loanAccountId <= 0) {
            throw new IllegalArgumentException("Invalid loan account ID");
        }

        log.info("Fetching penalty and bonus charges for loanAccountId: {}", loanAccountId);

        return penaltyRepo.findByLoanAccountId(loanAccountId)
                .stream()
                .map(p -> mapper.map(p, PenaltyResponse.class))
                .toList();
    }

    // Get single penalty/bonus detail
    @Override
    public PenaltyResponse getPenalty(Integer penaltyChargeId) {

        if (penaltyChargeId == null || penaltyChargeId <= 0) {
            throw new IllegalArgumentException("Invalid penalty charge ID");
        }

        log.info("Fetching penalty charge with ID: {}", penaltyChargeId);

        PenaltyCharge penalty = penaltyRepo.findById(penaltyChargeId)
                .orElseThrow(() -> {
                    log.warn("Penalty charge not found with ID: {}", penaltyChargeId);
                    return new PenaltyNotFoundException(
                            "Penalty charge not found with ID: " + penaltyChargeId);
                });

        return mapper.map(penalty, PenaltyResponse.class);
    }
}
