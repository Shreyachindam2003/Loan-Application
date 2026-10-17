
package com.example.Loan.main.serviceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import com.example.Loan.main.dto.ClosureResponseDto;
import com.example.Loan.main.dto.ForeClosureRequestDto;
import com.example.Loan.main.dto.LoanClosureDto;
import com.example.Loan.main.entity.ForeClosureRequest;
import com.example.Loan.main.entity.LoanAccount;
import com.example.Loan.main.entity.LoanClosure;
import com.example.Loan.main.entity.LoanPayment;
import com.example.Loan.main.respository.ForeClosureRequestRepository;
import com.example.Loan.main.respository.LoanAccountRepository;
import com.example.Loan.main.respository.LoanClosureRepository;
import com.example.Loan.main.respository.LoanPaymentRepository;
import com.example.Loan.main.service.ClosureService;
import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ClosureServiceImpl implements ClosureService {

    private static final Logger log =
            LoggerFactory.getLogger(ClosureServiceImpl.class);

    private final ForeClosureRequestRepository requestRepository;
    private final LoanClosureRepository closureRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final LoanPaymentRepository paymentRepository;

    public ClosureServiceImpl(
            ForeClosureRequestRepository requestRepository,
            LoanClosureRepository closureRepository,
            LoanAccountRepository loanAccountRepository,
            LoanPaymentRepository paymentRepository) {

        this.requestRepository = requestRepository;
        this.closureRepository = closureRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public ClosureResponseDto createRequest(ForeClosureRequestDto dto) {

        if (dto == null || dto.getLoanAccountId() == null) {
            throw new IllegalArgumentException(
                    "Loan account ID is required");
        }

        LoanAccount account = loanAccountRepository
                .findById(dto.getLoanAccountId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Loan account not found"));

        BigDecimal outstanding = account.getOutstandingPrincipal();

        if (outstanding == null
                || outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Loan has no outstanding principal");
        }

        if ("CLOSED".equalsIgnoreCase(account.getLoanStatus())) {
            throw new IllegalArgumentException(
                    "Loan account is already closed");
        }

        if (dto.getForeClosureType() == null
                || dto.getForeClosureType().isBlank()) {
            throw new IllegalArgumentException(
                    "Closure type is required");
        }

        String type = dto.getForeClosureType()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!"FULL".equals(type) && !"PARTIAL".equals(type)) {
            throw new IllegalArgumentException(
                    "Closure type must be FULL or PARTIAL");
        }

        boolean duplicatePending = requestRepository
                .findByLoanAccountIdAndForeClosureTypeIgnoreCase(
                        dto.getLoanAccountId(), type)
                .stream()
                .anyMatch(r ->
                        "PENDING".equalsIgnoreCase(r.getStatus())
                                && !Boolean.TRUE.equals(r.getIsPaid()));

        if (duplicatePending) {
            throw new IllegalArgumentException(
                    "A pending closure request already exists "
                            + "for this loan and type");
        }

        ForeClosureRequest request = new ForeClosureRequest();

        request.setLoanAccountId(dto.getLoanAccountId());
        request.setForeClosureType(type);
        request.setRequestedDate(LocalDateTime.now());
        request.setReason(dto.getReason());
        request.setStatus("PENDING");
        request.setIsPaid(false);
        request.setPaidDate(null);
        request.setClosedBy(null);

        if ("FULL".equals(type)) {
            request.setForeClosureAmount(outstanding);
            request.setPartialAmount(null);
            request.setPartialTenureMonths(null);
            request.setRemainingPrincipal(BigDecimal.ZERO);
        } else {
            BigDecimal amount = dto.getPartialAmount();
            Integer months = dto.getPartialTenureMonths();

            if (amount == null
                    || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Partial amount must be greater than zero");
            }

            if (amount.compareTo(outstanding) >= 0) {
                throw new IllegalArgumentException(
                        "Partial amount must be less than outstanding principal");
            }

            if (months == null || months <= 0) {
                throw new IllegalArgumentException(
                        "Partial tenure months must be greater than zero");
            }

            request.setForeClosureAmount(null);
            request.setPartialAmount(amount);
            request.setPartialTenureMonths(months);
            request.setRemainingPrincipal(outstanding.subtract(amount));
        }

        ForeClosureRequest saved = requestRepository.save(request);

        ClosureResponseDto response = new ClosureResponseDto();
        response.setRequestId(saved.getRequestId());
        response.setLoanAccountId(saved.getLoanAccountId());
        response.setForeClosureType(saved.getForeClosureType());
        response.setForeClosureAmount(saved.getForeClosureAmount());
        response.setPartialAmount(saved.getPartialAmount());
        response.setRequestedDate(saved.getRequestedDate());
        response.setStatus(saved.getStatus());
        response.setMessage("Closure request created successfully");

        log.info("Created {} closure request {} for loan account {}",
                type, saved.getRequestId(), saved.getLoanAccountId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForeClosureRequest> getRequests() {
        return requestRepository.findAll();
    }

    @Override
    @Transactional
    public LoanClosure createClosure(LoanClosureDto dto) {

        if (dto == null || dto.getLoanAccountId() == null) {
            throw new IllegalArgumentException(
                    "Loan account ID is required");
        }

        BigDecimal settlement = dto.getFinalSettlementAmount();

        if (settlement == null
                || settlement.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Settlement amount must be greater than zero");
        }

        if (dto.getClosureType() == null
                || dto.getClosureType().isBlank()) {
            throw new IllegalArgumentException(
                    "Closure type is required");
        }

        String type = dto.getClosureType()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!"FULL".equals(type) && !"PARTIAL".equals(type)) {
            throw new IllegalArgumentException(
                    "Closure type must be FULL or PARTIAL");
        }

        LoanAccount account = loanAccountRepository
                .findById(dto.getLoanAccountId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Loan account not found"));

        BigDecimal outstanding = account.getOutstandingPrincipal();

        if (outstanding == null
                || outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Loan has no outstanding principal");
        }

        ForeClosureRequest request = requestRepository
                .findByLoanAccountIdAndForeClosureTypeIgnoreCase(
                        dto.getLoanAccountId(), type)
                .stream()
                .filter(r ->
                        "PENDING".equalsIgnoreCase(r.getStatus()))
                .filter(r ->
                        !Boolean.TRUE.equals(r.getIsPaid()))
                .filter(r -> {
                    BigDecimal expected = "FULL".equals(type)
                            ? r.getForeClosureAmount()
                            : r.getPartialAmount();

                    return expected != null
                            && expected.compareTo(settlement) == 0;
                })
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No matching pending closure request found"));

        List<LoanPayment> payments =
                paymentRepository.findByLoanAccountId(
                        dto.getLoanAccountId());

        boolean paymentVerified = payments.stream()
                .anyMatch(payment ->
                        payment.getPaymentAmount() != null
                                && payment.getPaymentAmount()
                                .compareTo(settlement) == 0
                                && "SUCCESS".equalsIgnoreCase(
                                payment.getPaymentStatus())
                                && payment.getPaymentDate() != null
                                && request.getRequestedDate() != null
                                && !payment.getPaymentDate().isBefore(
                                request.getRequestedDate()));

        if (!paymentVerified) {
            throw new IllegalArgumentException(
                    "Successful payment matching the closure amount was not found");
        }

        if ("FULL".equals(type)) {
            if (settlement.compareTo(outstanding) != 0) {
                throw new IllegalArgumentException(
                        "Full settlement must equal outstanding principal");
            }

            account.setOutstandingPrincipal(BigDecimal.ZERO);
            account.setLoanStatus("CLOSED");
        } else {
            if (settlement.compareTo(outstanding) >= 0) {
                throw new IllegalArgumentException(
                        "Partial settlement must be less than outstanding principal");
            }

            account.setOutstandingPrincipal(
                    outstanding.subtract(settlement));
            account.setLoanStatus("ACTIVE");
        }

        BigDecimal totalPaid = account.getTotalPaidAmount() == null
                ? BigDecimal.ZERO
                : account.getTotalPaidAmount();

        account.setTotalPaidAmount(totalPaid.add(settlement));
        loanAccountRepository.save(account);

        request.setStatus("COMPLETED");
        request.setIsPaid(true);
        request.setPaidDate(LocalDateTime.now());
        request.setClosedBy(dto.getClosedBy());
        requestRepository.save(request);

        LoanClosure closure = new LoanClosure();
        closure.setLoanAccountId(dto.getLoanAccountId());
        closure.setClosureType(type);
        closure.setFinalSettlementAmount(settlement);
        closure.setClosureDate(LocalDateTime.now());
        closure.setClosedBy(dto.getClosedBy());
        closure.setRemarks(dto.getRemarks());
        closure.setClosureStatus("COMPLETED");
        closure.setIsProcessed(false);

        LoanClosure saved = closureRepository.save(closure);

        log.info("Completed {} closure {} for loan account {}",
                type, saved.getClosureId(), dto.getLoanAccountId());

        return saved;
    }




    @Override
    @Transactional
    public ClosureResponseDto approveRequest(Integer requestId) {

        ForeClosureRequest request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Closure request not found: " + requestId));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())
                || Boolean.TRUE.equals(request.getIsPaid())) {
            throw new IllegalArgumentException(
                    "Only pending unpaid requests can be approved");
        }

        request.setStatus("APPROVED");

        ForeClosureRequest saved = requestRepository.save(request);

        ClosureResponseDto response = new ClosureResponseDto();
        response.setRequestId(saved.getRequestId());
        response.setLoanAccountId(saved.getLoanAccountId());
        response.setForeClosureType(saved.getForeClosureType());
        response.setForeClosureAmount(saved.getForeClosureAmount());
        response.setPartialAmount(saved.getPartialAmount());
        response.setRequestedDate(saved.getRequestedDate());
        response.setStatus(saved.getStatus());
        response.setMessage("Closure request approved successfully");

        log.info("Closure request {} approved", requestId);

        return response;
    }

    @Override
    @Transactional
    public ClosureResponseDto rejectRequest(Integer requestId) {

        ForeClosureRequest request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Closure request not found: " + requestId));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())
                || Boolean.TRUE.equals(request.getIsPaid())) {
            throw new IllegalArgumentException(
                    "Only pending unpaid requests can be rejected");
        }

        request.setStatus("REJECTED");

        ForeClosureRequest saved = requestRepository.save(request);

        ClosureResponseDto response = new ClosureResponseDto();
        response.setRequestId(saved.getRequestId());
        response.setLoanAccountId(saved.getLoanAccountId());
        response.setForeClosureType(saved.getForeClosureType());
        response.setForeClosureAmount(saved.getForeClosureAmount());
        response.setPartialAmount(saved.getPartialAmount());
        response.setRequestedDate(saved.getRequestedDate());
        response.setStatus(saved.getStatus());
        response.setMessage("Closure request rejected successfully");

        log.info("Closure request {} rejected", requestId);

        return response;
    }
}
