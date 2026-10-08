package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.entity.EmiPaymentEntity.LoanAccount;
import com.example.Loan.main.entity.EmiPaymentEntity.PenaltyCharge;
import com.example.Loan.main.enums.EmiPaymentEnums.LoanStatus;
import com.example.Loan.main.enums.EmiPaymentEnums.NotificationType;
import com.example.Loan.main.respository.EmiPaymentRepo.EmiScheduleRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.LoanAccountRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.PenaltyChargeRepository;
import com.example.Loan.main.service.EmiPaymentService.EmailService;
import com.example.Loan.main.service.EmiPaymentService.NotificationService;
import com.example.Loan.main.service.EmiPaymentService.SchedulerService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {

    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final PenaltyChargeRepository penaltyChargeRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    @Scheduled(cron="0 0 9 * * *")
    @Transactional
    public void processEmiReminders() {
        LocalDate today=LocalDate.now();
        sendReminder(today.plusDays(3),3);
        sendReminder(today.plusDays(2),2);
        sendReminder(today.plusDays(1),1);
    }

    private void sendReminder(LocalDate dueDate,int daysBefore) {
        List<EmiSchedule> emis=emiScheduleRepository.findPendingEmisBetweenDates("PENDING",dueDate,dueDate);

        for(EmiSchedule emi:emis) {
            LoanAccount loanAccount=loanAccountRepository
                    .findById(emi.getLoanAccountId()).orElse(null);
            if(loanAccount==null||!LoanStatus.ACTIVE.name().equals(loanAccount.getLoanStatus())) continue;

            String message="Your EMI of ₹"+emi.getEmi()+" is due on "+emi.getDueDate()
                    +". Please pay "+daysBefore+" day(s) before the due date.";

            notificationService.createNotification(
                    loanAccount.getCustomerId(),
                    message,
                    NotificationType.EMI_REMINDER,
                    emi.getEmiScheduleId()
            );
        }
    }

    @Override
    @Scheduled(cron="0 0 23 * * *")
    @Transactional
    public void processDailyPenalties() {
        LocalDate today=LocalDate.now();
        List<EmiSchedule> overdueEmis=emiScheduleRepository.findOverdueEmis(today);

        for(EmiSchedule emi:overdueEmis) {
            LoanAccount loanAccount=loanAccountRepository.findById(emi.getLoanAccountId()).orElse(null);
            if(loanAccount==null||!LoanStatus.ACTIVE.name().equals(loanAccount.getLoanStatus())) continue;

            int daysLate=(int)(today.toEpochDay()-emi.getDueDate().toEpochDay());
            addDailyPenalty(emi,daysLate);
            addMonthlyBonus(emi,today);
        }
    }

    private void addDailyPenalty(EmiSchedule emi,int daysLate) {
        LocalDate today=LocalDate.now();

        boolean alreadyAdded=penaltyChargeRepository
                .existsByEmiScheduleIdAndChargeTypeAndChargeDate(
                        emi.getEmiScheduleId(),"PENALTY",today);

        if(alreadyAdded) return;

        PenaltyCharge penalty=new PenaltyCharge();
        penalty.setEmiScheduleId(emi.getEmiScheduleId());
        penalty.setLoanAccountId(emi.getLoanAccountId());
        penalty.setPenaltyAmount(BigDecimal.ONE);
        penalty.setReason("Late payment penalty");
        penalty.setStatus("PENDING");
        penalty.setCreatedAt(LocalDateTime.now());
        penalty.setChargeType("PENALTY");
        penalty.setChargeDate(today);
        penalty.setDaysLate(daysLate);

        penaltyChargeRepository.save(penalty);
    }

    private void addMonthlyBonus(EmiSchedule emi,LocalDate today) {
        LocalDate monthStart=today.withDayOfMonth(1);

        boolean alreadyAdded=penaltyChargeRepository
                .existsByEmiScheduleIdAndChargeTypeAndChargeDate(
                        emi.getEmiScheduleId(),"BONUS",monthStart);

        if(alreadyAdded) return;

        PenaltyCharge bonus=new PenaltyCharge();
        bonus.setEmiScheduleId(emi.getEmiScheduleId());
        bonus.setLoanAccountId(emi.getLoanAccountId());
        bonus.setPenaltyAmount(BigDecimal.valueOf(100));
        bonus.setReason("Monthly late payment bonus charge");
        bonus.setStatus("PENDING");
        bonus.setCreatedAt(LocalDateTime.now());
        bonus.setChargeType("BONUS");
        bonus.setChargeDate(monthStart);
        bonus.setDaysLate(0);

        penaltyChargeRepository.save(bonus);
    }

    @Override
    @Scheduled(cron="0 0 0 * * *")
    @Transactional
    public void checkOverdueLoans() {
        LocalDate today=LocalDate.now();

        List<EmiSchedule> overdueEmis=emiScheduleRepository.findOverdueEmis(today);

        Map<Integer,Long> missedEmiCount=overdueEmis.stream()
                .collect(Collectors.groupingBy(
                        EmiSchedule::getLoanAccountId,
                        Collectors.counting()
                ));

        List<LoanAccount> activeLoans=loanAccountRepository.findAll()
                .stream()
                .filter(loan->LoanStatus.ACTIVE.name().equals(loan.getLoanStatus()))
                .toList();

        for(LoanAccount loan:activeLoans) {
            long missedEmis=missedEmiCount.getOrDefault(loan.getLoanAccountId(),0L);
            if(missedEmis>=3) blockLoan(loan,(int)missedEmis);
        }
    }

    private void blockLoan(LoanAccount loan,int missedEmis) {
        loan.setLoanStatus(LoanStatus.BLOCKED.name());
        loanAccountRepository.save(loan);

        String message="Your loan account "+loan.getLoanAccountNo()
                +" has been blocked because "+missedEmis+" EMIs are overdue.";

        notificationService.createNotification(
                loan.getCustomerId(),
                message,
                NotificationType.ACCOUNT_BLOCKED,
                loan.getLoanAccountId()
        );
    }
}