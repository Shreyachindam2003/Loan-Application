
package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;


import com.example.Loan.main.entity.ForeClosureRequest;
import com.example.Loan.main.entity.loandealcreation.Disbursement;
import com.example.Loan.main.entity.loandealcreation.LoanAccount;
import com.example.Loan.main.entity.loandealcreation.LoanDeal;
import com.example.Loan.main.respository.ForeClosureRequestRepository;
import com.example.Loan.main.respository.loandealcreation.DisbursementRepository;
import com.example.Loan.main.respository.loandealcreation.LoanAccountRepository;
import com.example.Loan.main.respository.loandealcreation.LoanDealRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Loan.main.dto.EmiPaymentDto.response.EmiScheduleResponse;
import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.entity.EmiPaymentEntity.LoanClosure;
import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
import com.example.Loan.main.exception.InvalidLoanDataException;
import com.example.Loan.main.exception.LoanClosureException;
import com.example.Loan.main.exception.ResourceNotFoundException;
import com.example.Loan.main.respository.EmiPaymentRepo.EmiScheduleRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.LoanClosureRepository;
import com.example.Loan.main.service.EmiPaymentService.EmiScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmiScheduleServiceImpl implements EmiScheduleService {

    private final EmiScheduleRepository emiRepo;
    private final LoanAccountRepository accountRepo;
    private final LoanDealRepository dealRepo;
    private final DisbursementRepository disbursementRepo;
    private final LoanClosureRepository closureRepo;
    private final ForeClosureRequestRepository foreClosureRepo;
    private final ModelMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<EmiScheduleResponse> getEmiSchedule(Integer loanId) {

        if (!accountRepo.existsById(loanId)) {
            throw new ResourceNotFoundException(
                    "Loan account not found: " + loanId);
        }

        return emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId)
                .stream()
                .map(e -> mapper.map(e, EmiScheduleResponse.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmiScheduleResponse getEmiById(Integer id) {

        EmiSchedule emi = emiRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EMI schedule not found: " + id));

        return mapper.map(emi, EmiScheduleResponse.class);
    }

    @Override
    @Transactional
    public List<EmiScheduleResponse> generateEmiSchedule(Integer loanId) {

        log.info("Starting EMI schedule generation for Loan Account ID: {}",
                loanId);

        LoanAccount account = accountRepo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found: " + loanId));

        LoanDeal deal = dealRepo.findById(account.getDealId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan deal not found: " + account.getDealId()));

        Disbursement disbursement = disbursementRepo
                .findByDealId(account.getDealId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found for Deal ID: "
                                + account.getDealId()));

        if (disbursement.getDisbursementDate() == null) {
            throw new InvalidLoanDataException(
                    "Disbursement date is missing");
        }

        BigDecimal principalAmount = disbursement.getDisburseAmount();
        BigDecimal regularEmi = deal.getEmiAmount();
        BigDecimal annualInterestRate = deal.getInterestRate();

        if (principalAmount == null
                || regularEmi == null
                || annualInterestRate == null
                || deal.getTenureMonths() == null
                || deal.getEmiDay() == null) {

            throw new InvalidLoanDataException(
                    "Loan or disbursement details are incomplete");
        }

        if (principalAmount.compareTo(BigDecimal.ZERO) <= 0
                || regularEmi.compareTo(BigDecimal.ZERO) <= 0
                || deal.getTenureMonths() <= 0
                || deal.getEmiDay() < 1
                || deal.getEmiDay() > 31
                || annualInterestRate.compareTo(BigDecimal.ZERO) < 0) {

            throw new InvalidLoanDataException(
                    "Invalid disbursed amount, EMI, interest rate, tenure or EMI day");
        }

        List<EmiSchedule> list =
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);

        if (list.isEmpty()) {

            log.info("Generating new EMI schedule for Loan Account ID: {}",
                    loanId);

            createSchedule(
                    loanId,
                    principalAmount,
                    regularEmi,
                    annualInterestRate,
                    deal.getTenureMonths(),
                    deal.getEmiDay(),
                    disbursement.getDisbursementDate().toLocalDate());

            list = emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);

            log.info("EMI schedule generated. Installments: {}", list.size());

        } else {

            log.info(
                    "EMI schedule already exists. Loan Account ID: {}, Installments: {}",
                    loanId, list.size());

            if (list.size() != deal.getTenureMonths()) {
                log.warn(
                        "Existing schedule count: {}, Original tenure: {}",
                        list.size(), deal.getTenureMonths());
            }
        }

        LoanClosure closure = closureRepo
                .findFirstByLoanAccountIdAndClosureStatusAndIsProcessedFalseOrderByClosureDateAsc(
                        loanId, "COMPLETED")
                .orElse(null);

        if (closure == null) {
            return list.stream()
                    .map(e -> mapper.map(e, EmiScheduleResponse.class))
                    .toList();
        }

        log.info("Processing closure. Closure ID: {}, Type: {}",
                closure.getClosureId(), closure.getClosureType());

        if ("FULL".equalsIgnoreCase(closure.getClosureType())) {

            list.stream()
                    .filter(e -> e.getStatus() == EmiStatus.PENDING)
                    .forEach(e -> e.setStatus(EmiStatus.CLOSED));

        } else if ("PARTIAL".equalsIgnoreCase(closure.getClosureType())) {

            BigDecimal outstanding = calculateOutstanding(
                    loanId, list, principalAmount);

            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {

                list.stream()
                        .filter(e -> e.getStatus() == EmiStatus.PENDING)
                        .forEach(e -> e.setStatus(EmiStatus.CLOSED));

            } else {

                recalculate(
                        list,
                        outstanding,
                        annualInterestRate,
                        regularEmi);
            }

        } else {

            throw new LoanClosureException(
                    "Unsupported loan closure type: "
                            + closure.getClosureType());
        }

        emiRepo.saveAll(list);

        closure.setIsProcessed(true);
        closureRepo.save(closure);

        return emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId)
                .stream()
                .map(e -> mapper.map(e, EmiScheduleResponse.class))
                .toList();
    }

    @Override
    @Transactional
    public List<EmiScheduleResponse> recalculateEmiSchedule(Integer loanId) {

        log.info("Partial closure EMI recalculation started for loanId: {}",
                loanId);

        LoanAccount account = accountRepo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found: " + loanId));

        LoanDeal deal = dealRepo.findById(account.getDealId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan deal not found: " + account.getDealId()));

        List<EmiSchedule> list =
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);

        LoanClosure closure = closureRepo
                .findFirstByLoanAccountIdAndClosureStatusAndIsProcessedFalseOrderByClosureDateAsc(
                        loanId, "COMPLETED")
                .orElseThrow(() -> new LoanClosureException(
                        "No unprocessed completed loan closure found for loanId: "
                                + loanId));

        if (!"PARTIAL".equalsIgnoreCase(closure.getClosureType())) {
            throw new LoanClosureException(
                    "EMI recalculation is allowed only after partial closure");
        }

        Disbursement disbursement = disbursementRepo
                .findByDealId(account.getDealId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found for Deal ID: "
                                + account.getDealId()));

        BigDecimal originalPrincipal = disbursement.getDisburseAmount();
        BigDecimal regularEmi = deal.getEmiAmount();
        BigDecimal annualInterestRate = deal.getInterestRate();

        if (originalPrincipal == null
                || regularEmi == null
                || annualInterestRate == null) {

            throw new InvalidLoanDataException(
                    "Loan or EMI details are incomplete");
        }

        BigDecimal outstanding = calculateOutstanding(
                loanId, list, originalPrincipal);
        log.info("Final outstanding for recalculation: {}", outstanding);

        if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLoanDataException(
                    "Partial amount exceeds the outstanding principal");
        }

        if (outstanding.compareTo(BigDecimal.ZERO) == 0) {

            list.stream()
                    .filter(e -> e.getStatus() == EmiStatus.PENDING)
                    .forEach(e -> e.setStatus(EmiStatus.CLOSED));

            emiRepo.saveAll(list);

        } else {

            recalculate(
                    list,
                    outstanding,
                    annualInterestRate,
                    regularEmi);
        }

        account.setOutstandingPrincipal(outstanding);
        accountRepo.save(account);

        closure.setIsProcessed(true);
        closureRepo.save(closure);

        log.info("Partial closure recalculation completed for loanId: {}",
                loanId);

        return emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId)
                .stream()
                .map(e -> mapper.map(e, EmiScheduleResponse.class))
                .toList();
    }


    private BigDecimal calculateOutstanding(
            Integer loanId,
            List<EmiSchedule> list,
            BigDecimal originalPrincipal) {

        // 1. Principal already paid through EMIs
        BigDecimal paidPrincipal = list.stream()
                .filter(e -> e.getStatus() == EmiStatus.PAID)
                .map(EmiSchedule::getPrincipalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Get paid partial prepayment requests
        List<ForeClosureRequest> partialRequests =
                foreClosureRepo
                        .findByLoanAccountIdAndForeClosureTypeIgnoreCaseAndIsPaidTrue(
                                loanId, "PARTIAL");

        // 3. Total partial amount
        BigDecimal totalPartialAmount = partialRequests.stream()
                .map(ForeClosureRequest::getPartialAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Calculate outstanding
        BigDecimal outstanding = originalPrincipal
                .subtract(paidPrincipal)
                .subtract(totalPartialAmount);

        if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLoanDataException(
                    "Paid EMI principal and partial amounts exceed original principal");
        }

        log.info(
                "Outstanding calculation: loanId={}, originalPrincipal={}, paidPrincipal={}, partialAmount={}, outstanding={}",
                loanId, originalPrincipal, paidPrincipal,
                totalPartialAmount, outstanding);

        return outstanding.setScale(2, RoundingMode.HALF_UP);
    }


    private void createSchedule(
            Integer loanId,
            BigDecimal principalAmount,
            BigDecimal regularEmi,
            BigDecimal annualInterestRate,
            int tenure,
            int emiDay,
            LocalDate disbursementDate) {

        BigDecimal fixedInterest = principalAmount
                .multiply(rate(annualInterestRate))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal balance = principalAmount;
        YearMonth month = YearMonth.from(firstEmiDate(disbursementDate, emiDay));

        for (int i = 1; i <= tenure && balance.compareTo(BigDecimal.ZERO) > 0; i++) {
            LocalDate dueDate = getEmiDate(month, emiDay);
            BigDecimal principal = regularEmi.subtract(fixedInterest)
                    .setScale(2, RoundingMode.HALF_UP);

            if (principal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidLoanDataException(
                        "EMI must be greater than fixed monthly interest");
            }

            principal = principal.min(balance);
            BigDecimal currentEmi = principal.add(fixedInterest)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal closingBalance = balance.subtract(principal);

            EmiSchedule emi = new EmiSchedule();
            emi.setLoanAccountId(loanId);
            emi.setInstallmentNo(i);
            emi.setDueDate(dueDate);
            emi.setOpeningBalance(balance);
            emi.setEmiAmount(currentEmi);
            emi.setPrincipalAmount(principal);
            emi.setInterestAmount(fixedInterest);
            emi.setClosingBalance(closingBalance);
            emi.setStatus(EmiStatus.PENDING);

            emiRepo.save(emi);

            balance = closingBalance;
            month = month.plusMonths(1);
        }

        log.info("Created fixed-interest EMI schedule for loanId: {}", loanId);
    }



    private void recalculate(
            List<EmiSchedule> list,
            BigDecimal outstanding,
            BigDecimal annualInterestRate,
            BigDecimal regularEmi) {

        LocalDate today = LocalDate.now();

        List<EmiSchedule> future = list.stream()
                .filter(e -> e.getStatus() == EmiStatus.PENDING)
                .filter(e -> e.getDueDate() != null
                        && e.getDueDate().isAfter(today))
                .toList();

        BigDecimal originalPrincipal = list.stream()
                .filter(e -> e.getInstallmentNo() == 1)
                .map(EmiSchedule::getOpeningBalance)
                .findFirst()
                .orElseThrow(() -> new InvalidLoanDataException(
                        "Original principal not found"));

        BigDecimal fixedInterest = originalPrincipal
                .multiply(rate(annualInterestRate))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal balance = outstanding;

        for (EmiSchedule emi : future) {

            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                emi.setOpeningBalance(BigDecimal.ZERO);
                emi.setInterestAmount(BigDecimal.ZERO);
                emi.setPrincipalAmount(BigDecimal.ZERO);
                emi.setEmiAmount(BigDecimal.ZERO);
                emi.setClosingBalance(BigDecimal.ZERO);
                emi.setStatus(EmiStatus.CLOSED);
                continue;
            }

            BigDecimal principal = regularEmi.subtract(fixedInterest)
                    .setScale(2, RoundingMode.HALF_UP);

            if (principal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidLoanDataException(
                        "EMI must be greater than fixed monthly interest");
            }

            principal = principal.min(balance);
            BigDecimal currentEmi = principal.add(fixedInterest)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal closingBalance = balance.subtract(principal);

            emi.setOpeningBalance(balance);
            emi.setPrincipalAmount(principal);
            emi.setInterestAmount(fixedInterest);
            emi.setEmiAmount(currentEmi);
            emi.setClosingBalance(closingBalance);

            balance = closingBalance;
        }

        emiRepo.saveAll(future);

        log.info("Fixed-interest recalculation completed. Outstanding: {}, Remaining: {}",
                outstanding, balance);
    }


    private BigDecimal rate(BigDecimal annualRate) {
        return annualRate.divide(
                BigDecimal.valueOf(1200),
                10,
                RoundingMode.HALF_UP);
    }

    private LocalDate firstEmiDate(
            LocalDate disbursementDate,
            int emiDay) {

        LocalDate candidate = getEmiDate(
                YearMonth.from(disbursementDate), emiDay);

        if (candidate.isAfter(disbursementDate)) {
            return candidate;
        }

        return getEmiDate(
                YearMonth.from(disbursementDate).plusMonths(1),
                emiDay);
    }

    private LocalDate getEmiDate(YearMonth month, int emiDay) {
        int day = Math.min(emiDay, month.lengthOfMonth());
        return month.atDay(day);
    }
}
