
package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import com.example.Loan.main.entity.ForeClosureRequest;
import com.example.Loan.main.respository.ForeClosureRequestRepository;
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

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int MONEY_SCALE = 2;

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

        return toResponse(
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId));
    }

    @Override
    @Transactional(readOnly = true)
    public EmiScheduleResponse getEmiById(Integer id) {
        EmiSchedule emi = emiRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EMI schedule not found: " + id));

        return mapper.map(emi, EmiScheduleResponse.class);
    }

    /*@Override
    @Transactional
    public List<EmiScheduleResponse> generateEmiSchedule(Integer loanId) {

        log.info("Starting EMI schedule generation for loanId: {}", loanId);

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

        if (principalAmount.compareTo(ZERO) <= 0
                || regularEmi.compareTo(ZERO) <= 0
                || deal.getTenureMonths() <= 0
                || deal.getEmiDay() < 1
                || deal.getEmiDay() > 31
                || annualInterestRate.compareTo(ZERO) < 0) {
            throw new InvalidLoanDataException(
                    "Invalid disbursed amount, EMI, interest rate, tenure or EMI day");
        }

        List<EmiSchedule> list =
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);

        if (list.isEmpty()) {
            createSchedule(
                    loanId,
                    principalAmount,
                    regularEmi,
                    annualInterestRate,
                    deal.getTenureMonths(),
                    deal.getEmiDay(),
                    disbursement.getDisbursementDate().toLocalDate());

            list = emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);
        }

        LoanClosure closure = closureRepo
                .findFirstByLoanAccountIdAndClosureStatusAndIsProcessedFalseOrderByClosureDateAsc(
                        loanId, "COMPLETED")
                .orElse(null);

        if (closure == null) {
            return toResponse(list);
        }

        if ("FULL".equalsIgnoreCase(closure.getClosureType())) {

            list.stream()
                    .filter(e -> e.getStatus() == EmiStatus.PENDING)
                    .forEach(e -> e.setStatus(EmiStatus.CLOSED));

            account.setOutstandingPrincipal(money(ZERO));
            accountRepo.save(account);

        } else if ("PARTIAL".equalsIgnoreCase(closure.getClosureType())) {

            BigDecimal outstanding = calculateOutstanding(
                    loanId, list, principalAmount);

            if (outstanding.compareTo(ZERO) <= 0) {
                list.stream()
                        .filter(e -> e.getStatus() == EmiStatus.PENDING)
                        .forEach(e -> e.setStatus(EmiStatus.CLOSED));

                outstanding = money(ZERO);
            } else {
                recalculate(
                        list, outstanding, annualInterestRate, regularEmi);
            }

            account.setOutstandingPrincipal(outstanding);
            accountRepo.save(account);

        } else {
            throw new LoanClosureException(
                    "Unsupported loan closure type: "
                            + closure.getClosureType());
        }

        emiRepo.saveAll(list);

        closure.setIsProcessed(true);
        closureRepo.save(closure);

        return toResponse(
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId));
    }*/


    @Override
    @Transactional
    public List<EmiScheduleResponse> generateEmiSchedule(Integer loanId) {

        LoanAccount account = accountRepo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found: " + loanId));

        LoanDeal deal = dealRepo.findById(account.getDealId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan deal not found: " + account.getDealId()));

        Disbursement disbursement = disbursementRepo
                .findByDealId(account.getDealId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found for Deal ID: " + account.getDealId()));

        List<EmiSchedule> list =
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);

        // Existing schedule ko dobara generate mat karo
        if (list.isEmpty()) {

            BigDecimal principal = disbursement.getDisburseAmount();
            BigDecimal emi = deal.getEmiAmount();
            BigDecimal annualRate = deal.getInterestRate();

            if (principal == null || emi == null || annualRate == null
                    || deal.getTenureMonths() == null
                    || deal.getEmiDay() == null
                    || disbursement.getDisbursementDate() == null) {
                throw new InvalidLoanDataException(
                        "Loan or disbursement details are incomplete");
            }

            if (principal.signum() <= 0 || emi.signum() <= 0
                    || annualRate.signum() < 0
                    || deal.getTenureMonths() <= 0
                    || deal.getEmiDay() < 1 || deal.getEmiDay() > 31) {
                throw new InvalidLoanDataException(
                        "Invalid loan details for EMI generation");
            }

            createSchedule(
                    loanId,
                    principal,
                    emi,
                    annualRate,
                    deal.getTenureMonths(),
                    deal.getEmiDay(),
                    disbursement.getDisbursementDate().toLocalDate());

            list = emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);
        }

        // Existing closure processing ko preserve karo
        LoanClosure closure = closureRepo
                .findFirstByLoanAccountIdAndClosureStatusAndIsProcessedFalseOrderByClosureDateAsc(
                        loanId, "COMPLETED")
                .orElse(null);

        if (closure == null) {
            return toResponse(list);
        }

        BigDecimal originalPrincipal = disbursement.getDisburseAmount();

        if ("FULL".equalsIgnoreCase(closure.getClosureType())) {

            list.stream()
                    .filter(e -> e.getStatus() == EmiStatus.PENDING)
                    .forEach(e -> e.setStatus(EmiStatus.CLOSED));

            account.setOutstandingPrincipal(money(ZERO));

        } else if ("PARTIAL".equalsIgnoreCase(closure.getClosureType())) {

            BigDecimal outstanding = calculateOutstanding(
                    loanId, list, originalPrincipal);

            if (outstanding.compareTo(ZERO) <= 0) {
                list.stream()
                        .filter(e -> e.getStatus() == EmiStatus.PENDING)
                        .forEach(e -> e.setStatus(EmiStatus.CLOSED));

                outstanding = money(ZERO);
            } else {
                recalculate(
                        list, outstanding,
                        deal.getInterestRate(), deal.getEmiAmount());
            }

            account.setOutstandingPrincipal(outstanding);

        } else {
            throw new LoanClosureException(
                    "Unsupported closure type: " + closure.getClosureType());
        }

        emiRepo.saveAll(list);
        accountRepo.save(account);

        closure.setIsProcessed(true);
        closureRepo.save(closure);

        return toResponse(
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId));
    }


    @Override
    @Transactional
    public List<EmiScheduleResponse> recalculateEmiSchedule(Integer loanId) {

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

        if (outstanding.compareTo(ZERO) < 0) {
            throw new InvalidLoanDataException(
                    "Partial amount exceeds the outstanding principal");
        }

        if (outstanding.compareTo(ZERO) == 0) {
            list.stream()
                    .filter(e -> e.getStatus() == EmiStatus.PENDING)
                    .forEach(e -> e.setStatus(EmiStatus.CLOSED));

            emiRepo.saveAll(list);
        } else {
            recalculate(
                    list, outstanding, annualInterestRate, regularEmi);
        }

        account.setOutstandingPrincipal(outstanding);
        accountRepo.save(account);

        closure.setIsProcessed(true);
        closureRepo.save(closure);

        return toResponse(
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId));
    }

    private BigDecimal calculateOutstanding(
            Integer loanId,
            List<EmiSchedule> list,
            BigDecimal originalPrincipal) {

        BigDecimal paidPrincipal = list.stream()
                .filter(e -> e.getStatus() == EmiStatus.PAID)
                .map(EmiSchedule::getPrincipalAmount)
                .filter(amount -> amount != null)
                .reduce(ZERO, BigDecimal::add);

        List<ForeClosureRequest> partialRequests = foreClosureRepo
                .findByLoanAccountIdAndForeClosureTypeIgnoreCaseAndIsPaidTrue(
                        loanId, "PARTIAL");

        BigDecimal totalPartialAmount = partialRequests.stream()
                .map(ForeClosureRequest::getPartialAmount)
                .filter(amount -> amount != null)
                .reduce(ZERO, BigDecimal::add);

        // Assumes each paid partialAmount is entirely principal prepayment.
        BigDecimal outstanding = money(
                originalPrincipal
                        .subtract(paidPrincipal)
                        .subtract(totalPartialAmount));

        if (outstanding.compareTo(ZERO) < 0) {
            throw new InvalidLoanDataException(
                    "Paid EMI principal and partial amounts exceed original principal");
        }

        log.info(
                "Outstanding: loanId={}, original={}, paidPrincipal={}, partial={}, outstanding={}",
                loanId, originalPrincipal, paidPrincipal,
                totalPartialAmount, outstanding);

        return outstanding;
    }

    /*private void createSchedule(
            Integer loanId,
            BigDecimal principalAmount,
            BigDecimal regularEmi,
            BigDecimal annualInterestRate,
            int tenure,
            int emiDay,
            LocalDate disbursementDate) {

        BigDecimal monthlyRate = rate(annualInterestRate);
        BigDecimal balance = money(principalAmount);

        YearMonth month = YearMonth.from(
                firstEmiDate(disbursementDate, emiDay));

        for (int i = 1;
             i <= tenure && balance.compareTo(ZERO) > 0;
             i++) {

            LocalDate dueDate = getEmiDate(month, emiDay);

            // Reducing-balance interest: current balance × monthly rate.
            BigDecimal interest = money(balance.multiply(monthlyRate));

            BigDecimal principal = money(regularEmi.subtract(interest));

            if (principal.compareTo(ZERO) <= 0) {
                throw new InvalidLoanDataException(
                        "EMI must be greater than the monthly interest amount");
            }

            principal = principal.min(balance);

            BigDecimal currentEmi = money(principal.add(interest));
            BigDecimal closingBalance = money(balance.subtract(principal));

            EmiSchedule emi = new EmiSchedule();
            emi.setLoanAccountId(loanId);
            emi.setInstallmentNo(i);
            emi.setDueDate(dueDate);
            emi.setOpeningBalance(balance);
            emi.setEmiAmount(currentEmi);
            emi.setPrincipalAmount(principal);
            emi.setInterestAmount(interest);
            emi.setClosingBalance(closingBalance);
            emi.setStatus(EmiStatus.PENDING);

            emiRepo.save(emi);

            balance = closingBalance;
            month = month.plusMonths(1);
        }

        if (balance.compareTo(ZERO) > 0) {
            log.warn(
                    "Schedule ended with remaining principal {} for loanId {}",
                    balance, loanId);
        }

        log.info("Reducing-balance EMI schedule created for loanId: {}", loanId);
    }*/


    private void createSchedule(
            Integer loanId,
            BigDecimal principalAmount,
            BigDecimal regularEmi,
            BigDecimal annualInterestRate,
            int tenure,
            int emiDay,
            LocalDate disbursementDate) {

        BigDecimal monthlyRate = rate(annualInterestRate);
        BigDecimal balance = money(principalAmount);

        YearMonth month = YearMonth.from(
                firstEmiDate(disbursementDate, emiDay));

        for (int i = 1; i <= tenure && balance.signum() > 0; i++) {

            LocalDate dueDate = getEmiDate(month, emiDay);

            BigDecimal interest = money(balance.multiply(monthlyRate));
            BigDecimal principal = money(regularEmi.subtract(interest));

            if (principal.signum() <= 0) {
                throw new InvalidLoanDataException(
                        "EMI must be greater than monthly interest");
            }

            // Last installment mein remaining principal clear karo
            if (i == tenure || principal.compareTo(balance) > 0) {
                principal = balance;
            }

            BigDecimal currentEmi = money(principal.add(interest));
            BigDecimal closingBalance = money(balance.subtract(principal));

            EmiSchedule schedule = new EmiSchedule();
            schedule.setLoanAccountId(loanId);
            schedule.setInstallmentNo(i);
            schedule.setDueDate(dueDate);
            schedule.setOpeningBalance(balance);
            schedule.setInterestAmount(interest);
            schedule.setPrincipalAmount(principal);
            schedule.setEmiAmount(currentEmi);
            schedule.setClosingBalance(closingBalance);
            schedule.setStatus(EmiStatus.PENDING);

            emiRepo.save(schedule);

            balance = closingBalance;
            month = month.plusMonths(1);
        }

        log.info("EMI schedule generated for loanId: {}", loanId);
    }


    private void recalculate(
            List<EmiSchedule> list,
            BigDecimal outstanding,
            BigDecimal annualInterestRate,
            BigDecimal regularEmi) {

        LocalDate today = LocalDate.now();

        // Keep paid and overdue/due-today EMI records unchanged.
        // Recalculate only future pending installments.
        List<EmiSchedule> future = list.stream()
                .filter(e -> e.getStatus() == EmiStatus.PENDING)
                .filter(e -> e.getDueDate() != null
                        && e.getDueDate().isAfter(today))
                .toList();

        if (future.isEmpty()) {
            log.warn("No future pending EMIs available for recalculation.");
            return;
        }

        BigDecimal monthlyRate = rate(annualInterestRate);
        BigDecimal balance = money(outstanding);

        for (EmiSchedule emi : future) {

            if (balance.compareTo(ZERO) <= 0) {
                emi.setOpeningBalance(money(ZERO));
                emi.setInterestAmount(money(ZERO));
                emi.setPrincipalAmount(money(ZERO));
                emi.setEmiAmount(money(ZERO));
                emi.setClosingBalance(money(ZERO));
                emi.setStatus(EmiStatus.CLOSED);
                continue;
            }

            // Recalculate interest on the remaining principal every month.
            BigDecimal interest = money(balance.multiply(monthlyRate));
            BigDecimal principal = money(regularEmi.subtract(interest));

            if (principal.compareTo(ZERO) <= 0) {
                throw new InvalidLoanDataException(
                        "EMI must be greater than the monthly interest amount");
            }

            principal = principal.min(balance);

            BigDecimal currentEmi = money(principal.add(interest));
            BigDecimal closingBalance = money(balance.subtract(principal));

            emi.setOpeningBalance(balance);
            emi.setInterestAmount(interest);
            emi.setPrincipalAmount(principal);
            emi.setEmiAmount(currentEmi);
            emi.setClosingBalance(closingBalance);

            balance = closingBalance;
        }

        emiRepo.saveAll(future);

        log.info(
                "Reducing-balance recalculation completed. Starting outstanding={}, remaining={}",
                outstanding, balance);
    }

    private List<EmiScheduleResponse> toResponse(List<EmiSchedule> list) {
        return list.stream()
                .map(e -> mapper.map(e, EmiScheduleResponse.class))
                .toList();
    }

    private BigDecimal rate(BigDecimal annualRate) {
        return annualRate.divide(
                BigDecimal.valueOf(1200),
                10,
                RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
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
                YearMonth.from(disbursementDate).plusMonths(1), emiDay);
    }

    private LocalDate getEmiDate(YearMonth month, int emiDay) {
        int day = Math.min(emiDay, month.lengthOfMonth());
        return month.atDay(day);
    }
}
