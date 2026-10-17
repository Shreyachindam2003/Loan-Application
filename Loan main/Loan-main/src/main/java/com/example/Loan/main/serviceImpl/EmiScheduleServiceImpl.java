
package com.example.Loan.main.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Loan.main.dto.response.EmiScheduleResponse;
import com.example.Loan.main.entity.Disbursement;
import com.example.Loan.main.entity.ForeClosureRequest;
import com.example.Loan.main.entity.LoanAccount;
import com.example.Loan.main.entity.LoanDeal;
import com.example.Loan.main.entity.EmiSchedule;
import com.example.Loan.main.entity.LoanClosure;
import com.example.Loan.main.enums.EmiStatus;
import com.example.Loan.main.exception.InvalidLoanDataException;
import com.example.Loan.main.exception.LoanClosureException;
import com.example.Loan.main.exception.ResourceNotFoundException;
import com.example.Loan.main.respository.DisbursementRepository;
import com.example.Loan.main.respository.ForeClosureRequestRepository;
import com.example.Loan.main.respository.LoanAccountRepository;
import com.example.Loan.main.respository.LoanDealRepository;
import com.example.Loan.main.respository.EmiScheduleRepository;
import com.example.Loan.main.respository.LoanClosureRepository;
import com.example.Loan.main.service.EmiScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmiScheduleServiceImpl implements EmiScheduleService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final EmiScheduleRepository emiRepo;
    private final LoanAccountRepository accountRepo;
    private final LoanDealRepository dealRepo;
    private final DisbursementRepository disbursementRepo;
    private final LoanClosureRepository closureRepo;
    private final ForeClosureRequestRepository foreClosureRepo;
    private final ModelMapper mapper;

    //@Cacheable(value = "emiSchedules", key = "#loanAccountId")
    @Override
    @Transactional(readOnly = true)
    public List<EmiScheduleResponse> getEmiSchedule(Integer loanId) {
        if (!accountRepo.existsById(loanId)) {
            throw new ResourceNotFoundException("Loan account not found: " + loanId);
        }

        return toResponse(
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId));
    }

    /*@Transactional(readOnly = true)
    @Override
    public Page<EmiScheduleResponse> getEmiSchedule(
            Integer loanId, Pageable pageable) {

        if (!accountRepo.existsById(loanId)) {
            throw new ResourceNotFoundException(
                    "Loan account not found: " + loanId);
        }

        return emiRepo
                .findByLoanAccountIdOrderByInstallmentNoAsc(loanId, pageable)
                .map(e -> mapper.map(e, EmiScheduleResponse.class));
    }*/

    @Override
    @Transactional(readOnly = true)
    public EmiScheduleResponse getEmiById(Integer id) {
        EmiSchedule emi = emiRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("EMI schedule not found: " + id));

        return mapper.map(emi, EmiScheduleResponse.class);
    }

    //@CacheEvict(value = "emiSchedules", key = "#loanAccountId")
    @Override
    @Transactional
    public List<EmiScheduleResponse> generateEmiSchedule(Integer loanId) {

        LoanAccount account = accountRepo.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan account not found: " + loanId));

        LoanDeal deal = dealRepo.findById(account.getDealId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan deal not found: " + account.getDealId()));

        Disbursement disbursement = disbursementRepo.findByDealId(account.getDealId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Disbursement not found"));

        List<EmiSchedule> list =
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);

        // Generate schedule only when it does not already exist.
        if (list.isEmpty()) {
            if (disbursement.getDisburseAmount() == null
                    || deal.getEmiAmount() == null
                    || deal.getInterestRate() == null
                    || deal.getTenureMonths() == null
                    || deal.getEmiDay() == null
                    || disbursement.getDisbursementDate() == null) {
                throw new InvalidLoanDataException("Loan details are incomplete");
            }

            createSchedule(
                    loanId,
                    disbursement.getDisburseAmount(),
                    deal.getEmiAmount(),
                    deal.getInterestRate(),
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

            account.setOutstandingPrincipal(ZERO.setScale(2));

        } else if ("PARTIAL".equalsIgnoreCase(closure.getClosureType())) {
            BigDecimal outstanding = calculateOutstanding(
                    loanId, list, disbursement.getDisburseAmount());

            if (outstanding.compareTo(ZERO) == 0) {
                list.stream()
                        .filter(e -> e.getStatus() == EmiStatus.PENDING)
                        .forEach(e -> e.setStatus(EmiStatus.CLOSED));
            } else {
                recalculate(list, outstanding,
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
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan account not found: " + loanId));

        LoanDeal deal = dealRepo.findById(account.getDealId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Loan deal not found: " + account.getDealId()));

        Disbursement disbursement = disbursementRepo.findByDealId(account.getDealId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Disbursement not found"));

        List<EmiSchedule> list =
                emiRepo.findByLoanAccountIdOrderByInstallmentNoAsc(loanId);

        LoanClosure closure = closureRepo
                .findFirstByLoanAccountIdAndClosureStatusAndIsProcessedFalseOrderByClosureDateAsc(
                        loanId, "COMPLETED")
                .orElseThrow(() ->
                        new LoanClosureException("No unprocessed completed closure found"));

        if (!"PARTIAL".equalsIgnoreCase(closure.getClosureType())) {
            throw new LoanClosureException(
                    "Recalculation is allowed only after partial closure");
        }

        BigDecimal outstanding = calculateOutstanding(
                loanId, list, disbursement.getDisburseAmount());

        if (outstanding.compareTo(ZERO) == 0) {
            list.stream()
                    .filter(e -> e.getStatus() == EmiStatus.PENDING)
                    .forEach(e -> e.setStatus(EmiStatus.CLOSED));
            emiRepo.saveAll(list);
        } else {
            recalculate(list, outstanding,
                    deal.getInterestRate(), deal.getEmiAmount());
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

        BigDecimal partialAmount = foreClosureRepo
                .findByLoanAccountIdAndForeClosureTypeIgnoreCaseAndIsPaidTrue(
                        loanId, "PARTIAL")
                .stream()
                .map(ForeClosureRequest::getPartialAmount)
                .filter(amount -> amount != null)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal outstanding = money(
                originalPrincipal.subtract(paidPrincipal).subtract(partialAmount));

        if (outstanding.compareTo(ZERO) < 0) {
            throw new InvalidLoanDataException(
                    "Paid amounts exceed original principal");
        }

        log.info("Loan {} outstanding principal: {}", loanId, outstanding);
        return outstanding;
    }

    private void createSchedule(
            Integer loanId,
            BigDecimal principalAmount,
            BigDecimal regularEmi,
            BigDecimal annualInterestRate,
            int tenure,
            int emiDay,
            LocalDate disbursementDate) {

        BigDecimal balance = money(principalAmount);
        BigDecimal monthlyRate = rate(annualInterestRate);

        YearMonth month = YearMonth.from(firstEmiDate(disbursementDate, emiDay));

        for (int i = 1; i <= tenure && balance.signum() > 0; i++) {

            LocalDate dueDate = getEmiDate(month, emiDay);
            BigDecimal interest = money(balance.multiply(monthlyRate));
            BigDecimal principal = money(regularEmi.subtract(interest));

            if (principal.signum() <= 0) {
                throw new InvalidLoanDataException(
                        "EMI must be greater than monthly interest");
            }

            if (i == tenure || principal.compareTo(balance) > 0) {
                principal = balance;
            }

            BigDecimal currentEmi = money(principal.add(interest));
            BigDecimal closingBalance = money(balance.subtract(principal));

            EmiSchedule emi = new EmiSchedule();
            emi.setLoanAccountId(loanId);
            emi.setInstallmentNo(i);
            emi.setDueDate(dueDate);
            emi.setOpeningBalance(balance);
            emi.setInterestAmount(interest);
            emi.setPrincipalAmount(principal);
            emi.setEmiAmount(currentEmi);
            emi.setClosingBalance(closingBalance);
            emi.setStatus(EmiStatus.PENDING);

            emiRepo.save(emi);

            balance = closingBalance;
            month = month.plusMonths(1);
        }

        log.info("EMI schedule generated for loan {}", loanId);
    }

    private void recalculate(
            List<EmiSchedule> list,
            BigDecimal outstanding,
            BigDecimal annualInterestRate,
            BigDecimal regularEmi) {

        List<EmiSchedule> future = list.stream()
                .filter(e -> e.getStatus() == EmiStatus.PENDING)
                .filter(e -> e.getDueDate() != null
                        && e.getDueDate().isAfter(LocalDate.now()))
                .toList();

        if (future.isEmpty()) {
            log.info("No future pending EMIs to recalculate");
            return;
        }

        BigDecimal balance = money(outstanding);
        BigDecimal monthlyRate = rate(annualInterestRate);

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

            BigDecimal interest = money(balance.multiply(monthlyRate));
            BigDecimal principal = money(regularEmi.subtract(interest));

            if (principal.signum() <= 0) {
                throw new InvalidLoanDataException(
                        "EMI must be greater than monthly interest");
            }

            principal = principal.min(balance);

            emi.setOpeningBalance(balance);
            emi.setInterestAmount(interest);
            emi.setPrincipalAmount(principal);
            emi.setEmiAmount(money(principal.add(interest)));
            emi.setClosingBalance(money(balance.subtract(principal)));

            balance = emi.getClosingBalance();
        }

        emiRepo.saveAll(future);
        log.info("Future EMIs recalculated. Remaining balance: {}", balance);
    }

    private List<EmiScheduleResponse> toResponse(List<EmiSchedule> list) {
        return list.stream()
                .map(e -> mapper.map(e, EmiScheduleResponse.class))
                .toList();
    }

    private BigDecimal rate(BigDecimal annualRate) {
        return annualRate.divide(
                BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDate firstEmiDate(LocalDate disbursementDate, int emiDay) {
        YearMonth month = YearMonth.from(disbursementDate);
        LocalDate candidate = getEmiDate(month, emiDay);

        return candidate.isAfter(disbursementDate)
                ? candidate
                : getEmiDate(month.plusMonths(1), emiDay);
    }

    private LocalDate getEmiDate(YearMonth month, int emiDay) {
        return month.atDay(Math.min(emiDay, month.lengthOfMonth()));
    }
}
