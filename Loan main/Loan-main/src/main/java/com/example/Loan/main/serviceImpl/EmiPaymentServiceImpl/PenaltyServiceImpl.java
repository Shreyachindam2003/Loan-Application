package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyBreakupResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyDetailResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyResponse;
import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.entity.EmiPaymentEntity.PenaltyCharge;
import com.example.Loan.main.exception.EmiPaymentException.ResourceNotFoundException;
import com.example.Loan.main.respository.EmiPaymentRepo.EmiScheduleRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.PenaltyChargeRepository;
import com.example.Loan.main.service.EmiPaymentService.PenaltyService;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PenaltyServiceImpl implements PenaltyService {

    private final PenaltyChargeRepository penaltyChargeRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly=true)
    public Page<PenaltyResponse> getPenalties(Integer loanAccountId,Pageable pageable) {

        Page<PenaltyCharge> charges=penaltyChargeRepository
                .findByLoanAccountIdOrderByPenaltyChargeIdDesc(loanAccountId,pageable);

        return charges.map(this::mapPenaltyResponse);
    }

    @Override
    @Transactional(readOnly=true)
    public PenaltyBreakupResponse getPenaltyBreakup(Integer emiScheduleId) {

        EmiSchedule emi=emiScheduleRepository.findById(emiScheduleId)
                .orElseThrow(()->new ResourceNotFoundException("EMI schedule not found"));

        List<PenaltyCharge> charges=penaltyChargeRepository
                .findByEmiScheduleIdOrderByChargeDateAsc(emiScheduleId);

        BigDecimal penaltyAmount=charges.stream()
                .filter(c->"PENALTY".equals(c.getChargeType())&&"PENDING".equals(c.getStatus()))
                .map(PenaltyCharge::getPenaltyAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal bonusAmount=charges.stream()
                .filter(c->"BONUS".equals(c.getChargeType())&&"PENDING".equals(c.getStatus()))
                .map(PenaltyCharge::getPenaltyAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        List<PenaltyDetailResponse> penaltyDetails=charges.stream()
                .filter(c->"PENALTY".equals(c.getChargeType())&&"PENDING".equals(c.getStatus()))
                .map(this::mapPenaltyDetail)
                .toList();

        List<PenaltyDetailResponse> bonusDetails=charges.stream()
                .filter(c->"BONUS".equals(c.getChargeType())&&"PENDING".equals(c.getStatus()))
                .map(this::mapPenaltyDetail)
                .toList();

        BigDecimal totalAmount=emi.getEmi()
                .add(penaltyAmount)
                .add(bonusAmount);

        return PenaltyBreakupResponse.builder()
                .emiAmount(emi.getEmi())
                .penaltyAmount(penaltyAmount)
                .bonusAmount(bonusAmount)
                .totalAmount(totalAmount)
                .penaltyDetails(penaltyDetails)
                .bonusDetails(bonusDetails)
                .build();
    }

    @Override
    @Transactional(readOnly=true)
    public PenaltyDetailResponse getPenaltyById(Integer penaltyChargeId) {

        PenaltyCharge charge=penaltyChargeRepository.findById(penaltyChargeId)
                .orElseThrow(()->new ResourceNotFoundException("Penalty charge not found"));

        return mapPenaltyDetail(charge);
    }

    private PenaltyResponse mapPenaltyResponse(PenaltyCharge charge) {

        EmiSchedule emi=emiScheduleRepository.findById(charge.getEmiScheduleId())
                .orElse(null);

        Integer installmentNo=null;
        LocalDate dueDate=null;

        if(emi!=null){
            installmentNo=emi.getInstallmentNo();
            dueDate=emi.getDueDate();
        }

        BigDecimal penaltyAmount=BigDecimal.ZERO;
        BigDecimal bonusAmount=BigDecimal.ZERO;

        if("PENALTY".equals(charge.getChargeType())){
            penaltyAmount=charge.getPenaltyAmount();
        }

        if("BONUS".equals(charge.getChargeType())){
            bonusAmount=charge.getPenaltyAmount();
        }

        return PenaltyResponse.builder()
                .penaltyChargeId(charge.getPenaltyChargeId())
                .emiScheduleId(charge.getEmiScheduleId())
                .installmentNo(installmentNo)
                .dueDate(dueDate)
                .penaltyAmount(penaltyAmount)
                .bonusAmount(bonusAmount)
                .totalCharges(penaltyAmount.add(bonusAmount))
                .status(charge.getStatus())
                .build();
    }

    private PenaltyDetailResponse mapPenaltyDetail(PenaltyCharge charge) {
        return modelMapper.map(charge,PenaltyDetailResponse.class);
    }
}