
package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.time.LocalDate;
import java.util.List;

import com.example.Loan.main.entity.Customer;
import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
import com.example.Loan.main.exception.EmiReminderException;
import com.example.Loan.main.respository.CustomerRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.EmiScheduleRepository;
import com.example.Loan.main.respository.LoanAccountRepository;

import com.example.Loan.main.service.EmiPaymentService.EmailService;
import com.example.Loan.main.service.EmiPaymentService.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmiReminderScheduler {

    private final EmiScheduleRepository emiRepo;
    private final LoanAccountRepository accountRepo;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendEmiReminders() {

        log.info("EMI reminder scheduler started");

        List<EmiSchedule> emis;

        try {
            emis = emiRepo.findByStatus(EmiStatus.PENDING);
        } catch (DataAccessException ex) {
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

                // Reminder exactly 3 days before EMI due date
                if (!dueDate.equals(today.plusDays(3))) {
                    continue;
                }

                Integer customerId = getCustomerId(
                        emi.getLoanAccountId());

                Customer customer = customerRepository
                        .findById(customerId)
                        .orElseThrow(() ->
                                new EmiReminderException(
                                        "Customer not found. Customer ID: "
                                                + customerId));

                String message =
                        "Your EMI of Rs. " + emi.getEmiAmount()
                                + " is due on " + dueDate
                                + ". Please pay before the due date.";

                // Create in-app reminder with duplicate prevention
                boolean created = notificationService.createEmiReminder(
                        customerId,
                        "EMI Payment Reminder",
                        message,
                        emi.getEmiScheduleId());

                // Send email only when a new reminder is created
                if (created) {
                    emailService.sendEmail(
                            customer.getEmail(),
                            "EMI Payment Reminder",
                            "Dear Customer,\n\n"
                                    + message
                                    + "\n\nRegards,\n"
                                    + "Loan Management Team");

                    successCount++;

                    log.info(
                            "EMI reminder created. EMI ID: {}, Loan Account ID: {}, Customer ID: {}",
                            emi.getEmiScheduleId(),
                            emi.getLoanAccountId(),
                            customerId);
                } else {
                    log.info(
                            "Duplicate reminder skipped for EMI ID: {}",
                            emi.getEmiScheduleId());
                }

            } catch (EmiReminderException ex) {
                failureCount++;
                log.error(
                        "EMI reminder processing failed. EMI ID: {}, Reason: {}",
                        emi.getEmiScheduleId(),
                        ex.getMessage(), ex);

            } catch (DataAccessException ex) {
                failureCount++;
                log.error(
                        "Database error while processing EMI ID: {}",
                        emi.getEmiScheduleId(), ex);

            } catch (RuntimeException ex) {
                failureCount++;
                log.error(
                        "Reminder or email sending failed for EMI ID: {}",
                        emi.getEmiScheduleId(), ex);
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
