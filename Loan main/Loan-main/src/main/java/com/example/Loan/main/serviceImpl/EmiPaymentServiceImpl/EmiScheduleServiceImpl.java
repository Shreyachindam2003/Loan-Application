package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import com.example.Loan.main.dto.EmiPaymentDto.response.EmiDashboardResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.EmiScheduleResponse;
import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.entity.EmiPaymentEntity.LoanAccount;
import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
import com.example.Loan.main.enums.EmiPaymentEnums.LoanStatus;
import com.example.Loan.main.exception.EmiPaymentException.BadRequestException;
import com.example.Loan.main.exception.EmiPaymentException.ResourceNotFoundException;
import com.example.Loan.main.respository.EmiPaymentRepo.EmiScheduleRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.LoanAccountRepository;
import com.example.Loan.main.service.EmiPaymentService.EmiScheduleService;
import com.example.Loan.main.util.EmiPaymentUtil.EmiCalculator;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmiScheduleServiceImpl implements EmiScheduleService {

    private final LoanAccountRepository loanAccountRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final EmiCalculator emiCalculator;
    private final ModelMapper modelMapper;


    // =========================================================
    // 1. EMI DASHBOARD
    // =========================================================

    @Override
    public EmiDashboardResponse getEmiDashboard(Integer loanAccountId) {

        LoanAccount loanAccount = getActiveLoan(loanAccountId);

        List<EmiSchedule> schedules =
                emiScheduleRepository
                        .findByLoanAccountIdOrderByInstallmentNoAsc(loanAccountId);

        long paidEmis = schedules.stream()
                .filter(e -> EmiStatus.PAID.name().equals(e.getPaymentStatus()))
                .count();

        long pendingEmis = schedules.stream()
                .filter(e -> EmiStatus.PENDING.name().equals(e.getPaymentStatus()))
                .count();

        BigDecimal paidAmount = schedules.stream()
                .filter(e -> EmiStatus.PAID.name().equals(e.getPaymentStatus()))
                .map(EmiSchedule::getEmi)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingBalance = schedules.stream()
                .filter(e -> EmiStatus.PENDING.name().equals(e.getPaymentStatus()))
                .map(EmiSchedule::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<EmiScheduleResponse> schedule =
                schedules.stream()
                        .map(this::toResponse)
                        .toList();

        EmiDashboardResponse response =
                modelMapper.map(loanAccount, EmiDashboardResponse.class);

        response.setTotalEmis(schedules.size());
        response.setPaidEmis((int) paidEmis);
        response.setPendingEmis((int) pendingEmis);
        response.setPaidAmount(paidAmount);
        response.setRemainingBalance(remainingBalance);
        response.setSchedule(schedule);

        return response;
    }


    // =========================================================
    // 2. GET EMI DAY
    // =========================================================

    @Override
    public Integer getEmiDay(Integer loanAccountId) {

        LoanAccount loanAccount =
                loanAccountRepository.findByLoanAccountId(loanAccountId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan account not found"
                                ));

        return loanAccount.getEmiDay();
    }


    // =========================================================
    // 3. GENERATE EMI SCHEDULE
    // =========================================================

    @Override
    @Transactional
    public void generateSchedule(Integer loanAccountId) {

        LoanAccount loanAccount = getActiveLoan(loanAccountId);

        if (loanAccount.getEmiDay() == null) {
            throw new BadRequestException("EMI day is not selected");
        }

        List<EmiSchedule> existing =
                emiScheduleRepository
                        .findByLoanAccountIdOrderByInstallmentNoAsc(
                                loanAccountId
                        );

        if (!existing.isEmpty()) {
            throw new BadRequestException(
                    "EMI schedule already generated"
            );
        }

        BigDecimal principal = loanAccount.getLoanAmount();
        BigDecimal rate = loanAccount.getInterestRate();
        int tenure = loanAccount.getTenureMonths();

        BigDecimal emi =
                emiCalculator.calculateEmi(
                        principal,
                        rate,
                        tenure
                );

        loanAccount.setEmiAmount(emi);
        loanAccountRepository.save(loanAccount);

        BigDecimal balance = principal;

        LocalDate firstDueDate =
                getFirstDueDate(loanAccount.getEmiDay());

        for (int i = 1; i <= tenure; i++) {

            LocalDate dueDate =
                    firstDueDate.plusMonths(i - 1);

            BigDecimal interest =
                    emiCalculator.calculateMonthlyInterest(
                            balance,
                            rate
                    );

            BigDecimal principalAmount =
                    emi.subtract(interest);

            BigDecimal currentEmi = emi;

            if (i == tenure) {
                principalAmount = balance;
                currentEmi = principalAmount.add(interest);
            }

            BigDecimal closingBalance =
                    balance.subtract(principalAmount)
                            .max(BigDecimal.ZERO);

            EmiSchedule schedule =
                    new EmiSchedule();

            schedule.setLoanAccountId(loanAccountId);
            schedule.setInstallmentNo(i);
            schedule.setDueDate(dueDate);
            schedule.setPrincipalAmount(principalAmount);
            schedule.setInterestAmount(interest);
            schedule.setOpeningBalance(balance);
            schedule.setClosingBalance(closingBalance);
            schedule.setEmi(currentEmi);
            schedule.setPaymentStatus(
                    EmiStatus.PENDING.name()
            );

            emiScheduleRepository.save(schedule);

            balance = closingBalance;
        }
    }


    // =========================================================
    // 4. FIRST EMI DATE
    // =========================================================

    private LocalDate getFirstDueDate(Integer emiDay) {

        LocalDate today = LocalDate.now();

        if (today.getDayOfMonth() <= emiDay) {
            return today.withDayOfMonth(emiDay);
        }

        return today.plusMonths(1).withDayOfMonth(emiDay);
    }


    // =========================================================
    // 5. GET ALL EMI SCHEDULE
    // =========================================================

    @Override
    public List<EmiScheduleResponse> getAllSchedules(
            Integer loanAccountId) {

        getActiveLoan(loanAccountId);

        List<EmiSchedule> schedules =
                emiScheduleRepository
                        .findByLoanAccountIdOrderByInstallmentNoAsc(
                                loanAccountId
                        );

        return schedules.stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // 6. GET EMI BY ID
    // =========================================================

    @Override
    public EmiScheduleResponse getScheduleById(
            Integer emiScheduleId) {

        EmiSchedule schedule =
                emiScheduleRepository.findById(emiScheduleId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "EMI schedule not found"
                                ));

        return toResponse(schedule);
    }


    // =========================================================
    // 7. GET ACTIVE LOAN
    // =========================================================

    private LoanAccount getActiveLoan(Integer loanAccountId) {

        return loanAccountRepository
                .findByLoanAccountIdAndLoanStatus(
                        loanAccountId,
                        LoanStatus.ACTIVE.name()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active loan account not found"
                        ));
    }


    // =========================================================
    // 8. MODEL MAPPER
    // =========================================================

    private EmiScheduleResponse toResponse(
            EmiSchedule schedule) {

        EmiScheduleResponse response =
                modelMapper.map(
                        schedule,
                        EmiScheduleResponse.class
                );

        response.setStatus(
                EmiStatus.valueOf(
                        schedule.getPaymentStatus()
                )
        );

        if (schedule.getPaidDate() != null) {
            response.setPaidDate(
                    schedule.getPaidDate().toLocalDate()
            );
        }

        return response;
    }


    // =========================================================
    // 9. PARTIAL FORECLOSURE
    // =========================================================

    @Override
    @Transactional
    public void recalculateAfterPartialForeclosure(
            Integer loanAccountId) {

        LoanAccount loanAccount =
                getActiveLoan(loanAccountId);

        BigDecimal principal =
                loanAccount.getOutstandingPrincipal();

        BigDecimal emi =
                loanAccount.getEmiAmount();

        BigDecimal rate =
                loanAccount.getInterestRate();

        if (principal == null ||
                principal.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BadRequestException(
                    "Outstanding principal must be greater than zero"
            );
        }

        List<EmiSchedule> allSchedules =
                emiScheduleRepository
                        .findByLoanAccountIdOrderByInstallmentNoAsc(
                                loanAccountId
                        );

        List<EmiSchedule> pendingSchedules =
                allSchedules.stream()
                        .filter(e ->
                                EmiStatus.PENDING.name()
                                        .equals(e.getPaymentStatus()))
                        .toList();

        if (pendingSchedules.isEmpty()) {
            throw new BadRequestException(
                    "No pending EMI schedule found"
            );
        }

        int remainingTenure =
                emiCalculator.calculateRemainingTenure(
                        principal,
                        emi,
                        rate
                );

        if (remainingTenure <= 0 ||
                remainingTenure > pendingSchedules.size()) {

            throw new BadRequestException(
                    "Invalid remaining tenure"
            );
        }

        LocalDate firstDueDate =
                pendingSchedules.get(0).getDueDate();

        BigDecimal balance = principal;

        for (int i = 0; i < remainingTenure; i++) {

            EmiSchedule schedule =
                    pendingSchedules.get(i);

            BigDecimal interest =
                    emiCalculator.calculateMonthlyInterest(
                            balance,
                            rate
                    );

            BigDecimal principalAmount =
                    emi.subtract(interest);

            BigDecimal currentEmi = emi;

            if (i == remainingTenure - 1) {
                principalAmount = balance;
                currentEmi = principalAmount.add(interest);
            }

            BigDecimal closingBalance =
                    balance.subtract(principalAmount)
                            .max(BigDecimal.ZERO);

            schedule.setDueDate(
                    firstDueDate.plusMonths(i)
            );
            schedule.setPrincipalAmount(principalAmount);
            schedule.setInterestAmount(interest);
            schedule.setOpeningBalance(balance);
            schedule.setClosingBalance(closingBalance);
            schedule.setEmi(currentEmi);

            balance = closingBalance;
        }

        for (int i = remainingTenure;
             i < pendingSchedules.size();
             i++) {

            pendingSchedules.get(i).setPaymentStatus(
                    EmiStatus.CANCELLED.name()
            );

            pendingSchedules.get(i).setCancellationReason(
                    "Cancelled due to partial foreclosure recalculation"
            );
        }

        emiScheduleRepository.saveAll(pendingSchedules);

        loanAccount.setTenureMonths(
                (int) allSchedules.stream()
                        .filter(e ->
                                EmiStatus.PAID.name()
                                        .equals(e.getPaymentStatus()))
                        .count()
                        + remainingTenure
        );

        loanAccountRepository.save(loanAccount);
    }


    // =========================================================
    // 10. FULL FORECLOSURE
    // =========================================================

    @Override
    @Transactional
    public void closeLoanAfterFullForeclosure(
            Integer loanAccountId) {

        LoanAccount loanAccount =
                getActiveLoan(loanAccountId);

        List<EmiSchedule> pendingSchedules =
                emiScheduleRepository
                        .findByLoanAccountIdOrderByInstallmentNoAsc(
                                loanAccountId
                        )
                        .stream()
                        .filter(e ->
                                EmiStatus.PENDING.name()
                                        .equals(e.getPaymentStatus()))
                        .toList();

        for (EmiSchedule schedule : pendingSchedules) {

            schedule.setPaymentStatus(
                    EmiStatus.CANCELLED.name()
            );

            schedule.setCancellationReason(
                    "Cancelled due to full foreclosure"
            );
        }

        emiScheduleRepository.saveAll(pendingSchedules);

        loanAccount.setOutstandingPrincipal(
                BigDecimal.ZERO
        );

        loanAccount.setLoanStatus(
                LoanStatus.CLOSED.name()
        );

        loanAccountRepository.save(loanAccount);
    }
}

