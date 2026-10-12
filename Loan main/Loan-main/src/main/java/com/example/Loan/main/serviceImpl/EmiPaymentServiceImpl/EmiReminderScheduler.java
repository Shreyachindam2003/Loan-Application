package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.time.LocalDate;
import java.util.List;

import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
import com.example.Loan.main.exception.EmiReminderException;
import com.example.Loan.main.respository.EmiPaymentRepo.EmiScheduleRepository;
import com.example.Loan.main.respository.LoanAccountRepository;
import com.example.Loan.main.service.EmiPaymentService.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmiReminderScheduler {

    private final EmiScheduleRepository emiRepo;
    private final LoanAccountRepository accountRepo;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendEmiReminders() {

        log.info("EMI reminder scheduler started");

        List<EmiSchedule> emis;

        try {
            emis = emiRepo.findByStatus(EmiStatus.PENDING);
        } catch (org.springframework.dao.DataAccessException ex) {
            log.error("Database error while fetching pending EMIs", ex);
            throw new EmiReminderException(
                    "Unable to fetch pending EMI schedules", ex);
        }

        LocalDate today = LocalDate.now();
        int successCount = 0;
        int failureCount = 0;

        for (EmiSchedule emi : emis) {
            try {
                LocalDate dueDate = emi.getDueDate();

                if (dueDate == null) {
                    throw new EmiReminderException(
                            "Due date missing for EMI ID: "
                                    + emi.getEmiScheduleId());
                }

                if (!dueDate.isBefore(today)
                        && !dueDate.isAfter(today.plusDays(3))) {

                    Integer customerId = getCustomerId(
                            emi.getLoanAccountId());

                    notificationService.createNotification(
                            customerId,
                            "EMI Reminder",
                            "Your EMI of ₹" + emi.getEmiAmount()
                                    + " is due on " + dueDate,
                            "EMI_REMINDER",
                            emi.getEmiScheduleId());

                    successCount++;

                    log.info(
                            "EMI reminder created successfully. EMI ID: {}, Loan Account ID: {}, Customer ID: {}",
                            emi.getEmiScheduleId(),
                            emi.getLoanAccountId(),
                            customerId);
                }

            } catch (EmiReminderException ex) {
                failureCount++;
                log.error(
                        "EMI reminder processing failed. EMI ID: {}, Loan Account ID: {}, Reason: {}",
                        emi.getEmiScheduleId(),
                        emi.getLoanAccountId(),
                        ex.getMessage(), ex);

            } catch (org.springframework.dao.DataAccessException ex) {
                failureCount++;
                log.error(
                        "Database error while processing EMI ID: {}, Loan Account ID: {}",
                        emi.getEmiScheduleId(),
                        emi.getLoanAccountId(), ex);

            } catch (RuntimeException ex) {
                failureCount++;
                log.error(
                        "Notification creation failed. EMI ID: {}, Loan Account ID: {}",
                        emi.getEmiScheduleId(),
                        emi.getLoanAccountId(), ex);
            }
        }

        log.info(
                "EMI reminder scheduler completed. Total pending EMIs: {}, Reminders created: {}, Failed: {}",
                emis.size(), successCount, failureCount);
    }

    private Integer getCustomerId(Integer loanAccountId) {

        return accountRepo.findById(loanAccountId)
                .map(account -> account.getCustomerId())
                .orElseThrow(() -> new EmiReminderException(
                        "Loan account not found. Loan Account ID: "
                                + loanAccountId));
    }
}

