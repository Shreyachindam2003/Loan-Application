package com.example.Loan.main.serviceImpl.closure;

import com.example.Loan.main.dto.closure.ClosureResponseDto;
import com.example.Loan.main.dto.closure.ForeClosureRequestDto;
import com.example.Loan.main.dto.closure.LoanClosureDto;
import com.example.Loan.main.dto.closure.LoanPaymentDto;
import com.example.Loan.main.entity.EmiPaymentEntity.LoanAccount;
import com.example.Loan.main.entity.closure.ForeClosureRequest;
import com.example.Loan.main.entity.closure.LoanClosure;
import com.example.Loan.main.entity.closure.LoanPayment;
import com.example.Loan.main.respository.EmiPaymentRepo.LoanAccountRepository;
import com.example.Loan.main.respository.closure.ForeClosureRequestRepository;
import com.example.Loan.main.respository.closure.LoanClosureRepository;
import com.example.Loan.main.respository.closure.LoanPaymentRepository;
import com.example.Loan.main.service.closure.ClosureService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClosureServiceImpl implements ClosureService {

    private final ForeClosureRequestRepository requestRepository;
    private final LoanPaymentRepository paymentRepository;
    private final LoanClosureRepository closureRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final ModelMapper modelMapper;

    public ClosureServiceImpl(
            ForeClosureRequestRepository requestRepository,
            LoanPaymentRepository paymentRepository,
            LoanClosureRepository closureRepository,
            LoanAccountRepository loanAccountRepository,
            ModelMapper modelMapper) {
        this.requestRepository = requestRepository;
        this.paymentRepository = paymentRepository;
        this.closureRepository = closureRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public ClosureResponseDto createRequest(ForeClosureRequestDto dto) {

        if (dto.getLoanAccountId() == null) {
            throw new IllegalArgumentException("Loan account ID is required");
        }

        LoanAccount account = loanAccountRepository
                .findById(dto.getLoanAccountId())
                .orElseThrow(() -> new RuntimeException("Loan account not found"));

        if (dto.getForeClosureType() == null
                || dto.getForeClosureType().isBlank()) {
            throw new IllegalArgumentException("Closure type is required");
        }

        String type = dto.getForeClosureType().trim().toUpperCase();

        ForeClosureRequest request = new ForeClosureRequest();
        request.setLoanAccountId(dto.getLoanAccountId());
        request.setForeClosureType(type);
        request.setReason(dto.getReason());
        request.setRequestedDate(LocalDateTime.now());
        request.setStatus("PENDING");
        request.setIsPaid(false);

        if ("FULL".equals(type)) {
            BigDecimal outstanding = account.getOutstandingPrincipal();

            if (outstanding == null || outstanding.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Outstanding principal must be greater than zero");
            }

            request.setForeClosureAmount(outstanding);
            request.setPartialAmount(null);
            request.setPartialTenureMonths(null);
            request.setRemainingPrincipal(BigDecimal.ZERO);
            request.setRevisedEmiAmount(BigDecimal.ZERO);

        } else if ("PARTIAL".equals(type)) {
            BigDecimal amount = dto.getPartialAmount();
            Integer months = dto.getPartialTenureMonths();
            BigDecimal outstanding = account.getOutstandingPrincipal();

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Partial amount must be greater than zero");
            }
            if (months == null || months <= 0) {
                throw new IllegalArgumentException("Partial tenure must be greater than zero");
            }
            if (outstanding == null || amount.compareTo(outstanding) >= 0) {
                throw new IllegalArgumentException("Partial amount must be less than outstanding principal");
            }

            BigDecimal remaining = outstanding.subtract(amount);
            request.setForeClosureAmount(null);
            request.setPartialAmount(amount);
            request.setPartialTenureMonths(months);
            request.setRemainingPrincipal(remaining);
            request.setRevisedEmiAmount(
                    calculateEmi(remaining, account.getInterestRate(), months)
            );

        } else {
            throw new IllegalArgumentException("Closure type must be FULL or PARTIAL");
        }

        ForeClosureRequest saved = requestRepository.save(request);
        ClosureResponseDto response = modelMapper.map(saved, ClosureResponseDto.class);
        response.setMessage("Closure request created successfully");
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForeClosureRequest> getRequests() {
        return requestRepository.findAll();
    }

    @Override
    @Transactional
    public LoanPayment createPayment(LoanPaymentDto dto) {

        if (dto.getLoanAccountId() == null) {
            throw new IllegalArgumentException("Loan account ID is required");
        }
        if (dto.getPaymentAmount() == null
                || dto.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        loanAccountRepository.findById(dto.getLoanAccountId())
                .orElseThrow(() -> new RuntimeException("Loan account not found"));

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

        if (dto.getLoanAccountId() == null) {
            throw new IllegalArgumentException("Loan account ID is required");
        }
        if (dto.getFinalSettlementAmount() == null
                || dto.getFinalSettlementAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Settlement amount must be greater than zero");
        }
        if (dto.getClosureType() == null || dto.getClosureType().isBlank()) {
            throw new IllegalArgumentException("Closure type is required");
        }

        LoanAccount account = loanAccountRepository
                .findById(dto.getLoanAccountId())
                .orElseThrow(() -> new RuntimeException("Loan account not found"));

        String type = dto.getClosureType().trim().toUpperCase();

        if (!"PARTIAL".equals(type) && !"FULL".equals(type)) {
            throw new IllegalArgumentException("Closure type must be FULL or PARTIAL");
        }

        // Require a successful payment with the same account and amount.
        // Note: the current schema cannot associate a payment with a specific request.
        boolean paymentExists = paymentRepository
                .findByLoanAccountId(dto.getLoanAccountId())
                .stream()
                .anyMatch(p ->
                        "SUCCESS".equalsIgnoreCase(p.getPaymentStatus())
                                && p.getPaymentAmount() != null
                                && p.getPaymentAmount().compareTo(
                                dto.getFinalSettlementAmount()) == 0);

        if (!paymentExists) {
            throw new IllegalArgumentException(
                    "No matching successful payment found for this loan and settlement amount");
        }

        ForeClosureRequest request = requestRepository
                .findByLoanAccountId(dto.getLoanAccountId())
                .stream()
                .filter(r -> type.equalsIgnoreCase(r.getForeClosureType()))
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                .filter(r -> Boolean.FALSE.equals(r.getIsPaid()))
                .filter(r -> {
                    BigDecimal expected = "FULL".equals(type)
                            ? r.getForeClosureAmount()
                            : r.getPartialAmount();
                    return expected != null
                            && expected.compareTo(dto.getFinalSettlementAmount()) == 0;
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No matching pending closure request found"));

        BigDecimal outstanding = account.getOutstandingPrincipal();

        if (outstanding == null || outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Loan has no outstanding principal");
        }

        if ("PARTIAL".equals(type)) {
            if (dto.getFinalSettlementAmount().compareTo(outstanding) >= 0) {
                throw new IllegalArgumentException(
                        "Partial settlement must be less than outstanding principal");
            }

            account.setOutstandingPrincipal(
                    outstanding.subtract(dto.getFinalSettlementAmount())
            );
            account.setLoanStatus("ACTIVE");

        } else {
            if (dto.getFinalSettlementAmount().compareTo(outstanding) != 0) {
                throw new IllegalArgumentException(
                        "Full settlement amount must equal outstanding principal");
            }

            account.setOutstandingPrincipal(BigDecimal.ZERO);
            account.setLoanStatus("CLOSED");
        }

        BigDecimal paidBefore = account.getTotalPaidAmount() == null
                ? BigDecimal.ZERO : account.getTotalPaidAmount();

        account.setTotalPaidAmount(
                paidBefore.add(dto.getFinalSettlementAmount())
        );

        loanAccountRepository.save(account);

        request.setStatus("COMPLETED");
        request.setIsPaid(true);
        request.setPaidDate(LocalDateTime.now());
        request.setClosedBy(dto.getClosedBy());
        requestRepository.save(request);

        LoanClosure closure = new LoanClosure();
        closure.setLoanAccountId(dto.getLoanAccountId());
        closure.setClosureType(type);
        closure.setFinalSettlementAmount(dto.getFinalSettlementAmount());
        closure.setClosedBy(dto.getClosedBy());
        closure.setRemarks(dto.getRemarks());
        closure.setClosureStatus("COMPLETED");
        closure.setClosureDate(LocalDateTime.now());

        return closureRepository.save(closure);
    }

    private BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal annualRate,
            int months) {

        if (months <= 0) {
            throw new IllegalArgumentException("Tenure months must be greater than zero");
        }
        if (principal == null || principal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Principal cannot be negative or null");
        }
        if (principal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(
                    BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        double monthlyRate = annualRate.doubleValue() / 12.0 / 100.0;
        double power = Math.pow(1.0 + monthlyRate, months);
        double emi = principal.doubleValue() * monthlyRate * power / (power - 1.0);

        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }
}
