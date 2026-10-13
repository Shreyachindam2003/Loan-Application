package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Loan.main.dto.EmiPaymentDto.response.EmiScheduleResponse;
import com.example.Loan.main.entity.Disbursement;
import com.example.Loan.main.entity.LoanAccount;
import com.example.Loan.main.entity.LoanDeal;
import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.entity.EmiPaymentEntity.LoanClosure;
import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
import com.example.Loan.main.exception.InvalidLoanDataException;
import com.example.Loan.main.exception.LoanClosureException;
import com.example.Loan.main.exception.ResourceNotFoundException;
import com.example.Loan.main.respository.DisbursementRepository;
import com.example.Loan.main.respository.LoanAccountRepository;
import com.example.Loan.main.respository.LoanDealRepository;
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

        if (principalAmount == null
                || deal.getInterestRate() == null
                || deal.getTenureMonths() == null
                || deal.getEmiDay() == null) {

            throw new InvalidLoanDataException(
                    "Loan or disbursement details are incomplete");
        }

        if (principalAmount.compareTo(BigDecimal.ZERO) <= 0
                || deal.getTenureMonths() <= 0
                || deal.getEmiDay() < 1
                || deal.getEmiDay() > 31
                || deal.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {

            throw new InvalidLoanDataException(
                    "Invalid disbursed amount, interest rate, tenure or EMI day");
        }

        List<EmiSchedule> list =
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);

        if (list.isEmpty()) {

            log.info("Generating new EMI schedule for Loan Account ID: {}",
                    loanId);

            createSchedule(
                    loanId,
                    principalAmount,
                    deal.getInterestRate(),
                    deal.getTenureMonths(),
                    deal.getEmiDay(),
                    disbursement.getDisbursementDate().toLocalDate());

            list = emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);

            log.info("EMI schedule generated. Installments: {}", list.size());

        } else {

            log.info("EMI schedule already exists. Loan Account ID: {}, Installments: {}",
                    loanId, list.size());

            if (list.size() != deal.getTenureMonths()) {
                log.warn(
                        "Incomplete EMI schedule. Existing: {}, Expected: {}",
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

            BigDecimal paidPrincipal = list.stream()
                    .filter(e -> e.getStatus() == EmiStatus.PAID)
                    .map(EmiSchedule::getPrincipalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal settlement =
                    closure.getFinalSettlementAmount() == null
                            ? BigDecimal.ZERO
                            : closure.getFinalSettlementAmount();

            BigDecimal outstanding = principalAmount
                    .subtract(paidPrincipal)
                    .subtract(settlement);

            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {

                list.stream()
                        .filter(e -> e.getStatus() == EmiStatus.PENDING)
                        .forEach(e -> e.setStatus(EmiStatus.CLOSED));

            } else {

                recalculate(list, outstanding, deal.getInterestRate());
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

        log.info("Partial closure EMI recalculation started for loanId: {}", loanId);

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
                        "No unprocessed completed loan closure found for loanId: " + loanId));

        if (!"PARTIAL".equalsIgnoreCase(closure.getClosureType())) {
            throw new LoanClosureException(
                    "EMI recalculation is allowed only after partial closure");
        }

        BigDecimal paidPrincipal = list.stream()
                .filter(e -> e.getStatus() == EmiStatus.PAID)
                .map(EmiSchedule::getPrincipalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal settlement = closure.getFinalSettlementAmount() == null
                ? BigDecimal.ZERO
                : closure.getFinalSettlementAmount();

        Disbursement disbursement = disbursementRepo
                .findByDealId(account.getDealId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found for Deal ID: " + account.getDealId()));

        BigDecimal originalPrincipal = disbursement.getDisburseAmount();

        if (originalPrincipal == null || settlement.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLoanDataException(
                    "Invalid disbursed amount or partial settlement amount");
        }

        BigDecimal outstanding = originalPrincipal
                .subtract(paidPrincipal)
                .subtract(settlement);

        if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLoanDataException(
                    "Partial settlement exceeds the outstanding principal");
        }

        if (outstanding.compareTo(BigDecimal.ZERO) == 0) {
            list.stream()
                    .filter(e -> e.getStatus() == EmiStatus.PENDING)
                    .forEach(e -> e.setStatus(EmiStatus.CLOSED));

            emiRepo.saveAll(list);
        } else {
            recalculate(list, outstanding, deal.getInterestRate());
        }

        closure.setIsProcessed(true);
        closureRepo.save(closure);

        log.info("Partial closure recalculation completed for loanId: {}", loanId);

        return emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId)
                .stream()
                .map(e -> mapper.map(e, EmiScheduleResponse.class))
                .toList();
    }


    private void createSchedule(
            Integer loanId,
            BigDecimal principalAmount,
            BigDecimal annualInterestRate,
            int tenure,
            int emiDay,
            LocalDate disbursementDate) {

        BigDecimal monthlyRate = rate(annualInterestRate);

        BigDecimal regularEmi = calculateEmi(
                principalAmount, monthlyRate, tenure);

        BigDecimal balance = principalAmount;

        LocalDate firstDate = firstEmiDate(disbursementDate, emiDay);
        YearMonth month = YearMonth.from(firstDate);

        for (int i = 1; i <= tenure; i++) {

            LocalDate dueDate = getEmiDate(month, emiDay);
            BigDecimal openingBalance = balance;

            BigDecimal interest = balance.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principal = regularEmi.subtract(interest)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal currentEmi = regularEmi;

            BigDecimal closingBalance = balance.subtract(principal)
                    .setScale(2, RoundingMode.HALF_UP);

            if (i == tenure
                    || closingBalance.compareTo(BigDecimal.ZERO) < 0) {

                principal = balance;
                currentEmi = principal.add(interest)
                        .setScale(2, RoundingMode.HALF_UP);
                closingBalance = BigDecimal.ZERO;
            }

            EmiSchedule emi = new EmiSchedule();
            emi.setLoanAccountId(loanId);
            emi.setInstallmentNo(i);
            emi.setDueDate(dueDate);
            emi.setOpeningBalance(openingBalance);
            emi.setEmiAmount(currentEmi);
            emi.setPrincipalAmount(principal);
            emi.setInterestAmount(interest);
            emi.setClosingBalance(closingBalance);
            emi.setStatus(EmiStatus.PENDING);

            emiRepo.save(emi);

            balance = closingBalance;
            month = month.plusMonths(1);
        }

        log.info("Created {} EMI installments for Loan Account ID: {}",
                tenure, loanId);
    }

    private void recalculate(
            List<EmiSchedule> list,
            BigDecimal outstanding,
            BigDecimal annualInterestRate) {

        LocalDate today = LocalDate.now();

        List<EmiSchedule> future = list.stream()
                .filter(e -> e.getStatus() == EmiStatus.PENDING)
                .filter(e -> e.getDueDate() != null
                        && e.getDueDate().isAfter(today))
                .toList();

        if (future.isEmpty()) {
            log.warn("No future pending EMIs available for recalculation");
            return;
        }

        BigDecimal monthlyRate = rate(annualInterestRate);

        BigDecimal regularEmi = calculateEmi(
                outstanding, monthlyRate, future.size());

        BigDecimal balance = outstanding;

        for (int i = 0; i < future.size(); i++) {

            EmiSchedule emi = future.get(i);

            BigDecimal interest = balance.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principal = regularEmi.subtract(interest)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal currentEmi = regularEmi;

            BigDecimal closingBalance = balance.subtract(principal)
                    .setScale(2, RoundingMode.HALF_UP);

            if (i == future.size() - 1
                    || closingBalance.compareTo(BigDecimal.ZERO) < 0) {

                principal = balance;
                currentEmi = principal.add(interest)
                        .setScale(2, RoundingMode.HALF_UP);
                closingBalance = BigDecimal.ZERO;
            }

            emi.setOpeningBalance(balance);
            emi.setEmiAmount(currentEmi);
            emi.setPrincipalAmount(principal);
            emi.setInterestAmount(interest);
            emi.setClosingBalance(closingBalance);

            balance = closingBalance;
        }

        emiRepo.saveAll(future);
    }

    private BigDecimal rate(BigDecimal annualRate) {
        return annualRate.divide(
                BigDecimal.valueOf(1200),
                10,
                RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal monthlyRate,
            int months) {

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(
                    BigDecimal.valueOf(months),
                    2,
                    RoundingMode.HALF_UP);
        }

        double r = monthlyRate.doubleValue();
        double factor = Math.pow(1.0 + r, months);

        return principal.multiply(monthlyRate)
                .multiply(BigDecimal.valueOf(factor))
                .divide(
                        BigDecimal.valueOf(factor - 1.0),
                        2,
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

