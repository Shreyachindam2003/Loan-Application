package com.example.loanapplication.serviceImpl.closure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loanapplication.dto.closure.ClosureResponseDto;
import com.example.loanapplication.dto.closure.ForeClosureRequestDto;
import com.example.loanapplication.dto.closure.LoanClosureDto;
import com.example.loanapplication.dto.closure.LoanPaymentDto;

import com.example.loanapplication.entity.EmiPaymentEntity.LoanAccount;

import com.example.loanapplication.entity.closure.ForeClosureRequest;
import com.example.loanapplication.entity.closure.LoanClosure;
import com.example.loanapplication.entity.closure.LoanPayment;

import com.example.loanapplication.repository.closure.ForeClosureRequestRepository;
import com.example.loanapplication.repository.closure.LoanClosureRepository;
import com.example.loanapplication.repository.closure.LoanPaymentRepository;

import com.example.loanapplication.respository.EmiPaymentRepo.LoanAccountRepository;

import com.example.loanapplication.service.closure.ClosureService;

@Service
public class ClosureServiceImpl implements ClosureService {

    private final ForeClosureRequestRepository requestRepository;
    private final LoanPaymentRepository paymentRepository;
    private final LoanClosureRepository closureRepository;
    private final LoanAccountRepository loanAccountRepository;

    public ClosureServiceImpl(
            ForeClosureRequestRepository requestRepository,
            LoanPaymentRepository paymentRepository,
            LoanClosureRepository closureRepository,
            LoanAccountRepository loanAccountRepository) {

        this.requestRepository = requestRepository;
        this.paymentRepository = paymentRepository;
        this.closureRepository = closureRepository;
        this.loanAccountRepository = loanAccountRepository;
    }

    @Override
    @Transactional
    public ClosureResponseDto createRequest(ForeClosureRequestDto dto) {

        LoanAccount account = loanAccountRepository
                .findById(dto.getLoanAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Loan account not found"));

        if (dto.getForeClosureType() == null) {
            throw new RuntimeException("Closure type is required");
        }

        ForeClosureRequest request = new ForeClosureRequest();

        request.setLoanAccountId(dto.getLoanAccountId());
        request.setForeClosureType(dto.getForeClosureType());
        request.setReason(dto.getReason());
        request.setRequestedDate(LocalDateTime.now());
        request.setStatus("PENDING");
        request.setIsPaid(false);

        if ("FULL".equalsIgnoreCase(dto.getForeClosureType())) {

            BigDecimal amount = account.getOutstandingPrincipal();

            request.setForeClosureAmount(amount);
            request.setPartialAmount(null);
            request.setPartialTenureMonths(null);
            request.setRemainingPrincipal(BigDecimal.ZERO);
            request.setRevisedEmiAmount(BigDecimal.ZERO);

        } else if ("PARTIAL".equalsIgnoreCase(dto.getForeClosureType())) {

            if (dto.getPartialAmount() == null ||
                    dto.getPartialAmount().compareTo(BigDecimal.ZERO) <= 0) {

                throw new RuntimeException(
                        "Partial amount must be greater than zero");
            }

            if (dto.getPartialTenureMonths() == null ||
                    dto.getPartialTenureMonths() <= 0) {

                throw new RuntimeException(
                        "Partial tenure must be greater than zero");
            }

            if (dto.getPartialAmount()
                    .compareTo(account.getOutstandingPrincipal()) >= 0) {

                throw new RuntimeException(
                        "Partial amount must be less than outstanding principal");
            }

            BigDecimal remainingPrincipal =
                    account.getOutstandingPrincipal()
                            .subtract(dto.getPartialAmount());

            BigDecimal revisedEmi = calculateEmi(
                    remainingPrincipal,
                    account.getInterestRate(),
                    dto.getPartialTenureMonths());

            request.setPartialAmount(dto.getPartialAmount());
            request.setPartialTenureMonths(dto.getPartialTenureMonths());
            request.setRemainingPrincipal(remainingPrincipal);
            request.setRevisedEmiAmount(revisedEmi);

        } else {

            throw new RuntimeException(
                    "Closure type must be FULL or PARTIAL");
        }

        ForeClosureRequest saved = requestRepository.save(request);

        ClosureResponseDto response = new ClosureResponseDto();

        response.setRequestId(saved.getRequestId());
        response.setLoanAccountId(saved.getLoanAccountId());
        response.setForeClosureType(saved.getForeClosureType());
        response.setPartialAmount(saved.getPartialAmount());
        response.setPartialTenureMonths(saved.getPartialTenureMonths());
        response.setRevisedEmiAmount(saved.getRevisedEmiAmount());
        response.setRemainingPrincipal(saved.getRemainingPrincipal());
        response.setStatus(saved.getStatus());
        response.setMessage("Closure request created successfully");

        return response;
    }

    @Override
    public List<ForeClosureRequest> getRequests() {
        return requestRepository.findAll();
    }

    @Override
    @Transactional
    public LoanPayment createPayment(LoanPaymentDto dto) {

        loanAccountRepository.findById(dto.getLoanAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Loan account not found"));

        LoanPayment payment = new LoanPayment();

        payment.setLoanAccountId(dto.getLoanAccountId());
        payment.setPaymentAmount(dto.getPaymentAmount());
        payment.setPaymentStatus(dto.getPaymentStatus());
        payment.setPaymentName(dto.getPaymentName());
        payment.setPaymentDate(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public LoanClosure createClosure(LoanClosureDto dto) {

        loanAccountRepository.findById(dto.getLoanAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Loan account not found"));

        LoanClosure closure = new LoanClosure();

        closure.setLoanAccountId(dto.getLoanAccountId());
        closure.setClosureType(dto.getClosureType());
        closure.setFinalSettlementAmount(
                dto.getFinalSettlementAmount());
        closure.setClosedBy(dto.getClosedBy());
        closure.setRemarks(dto.getRemarks());
        closure.setClosureStatus(dto.getClosureStatus());
        closure.setClosureDate(LocalDateTime.now());

        return closureRepository.save(closure);
    }

    private BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal annualRate,
            int months) {

        if (principal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(
                    BigDecimal.valueOf(months),
                    2,
                    RoundingMode.HALF_UP);
        }

        double monthlyRate =
                annualRate.doubleValue() / 12 / 100;

        double power =
                Math.pow(1 + monthlyRate, months);

        double emi =
                principal.doubleValue()
                        * monthlyRate
                        * power
                        / (power - 1);

        return BigDecimal.valueOf(emi)
                .setScale(2, RoundingMode.HALF_UP);
    }
}