package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

@Component
@RequiredArgsConstructor
@Slf4j
public class EmiReminderScheduler {

    private final EmiScheduleRepository emiRepo;
    private final LoanAccountRepository accountRepo;
    private final CustomerRepository customerRepo;
    private final NotificationService notificationService;
    private final EmailService emailService;

    // Run every day at 9 AM
//    @Scheduled(cron = "0 0 9 * * *")
    @Scheduled(cron = "0 * * * * *")
    public void sendEmiReminders() {

        LocalDate today = LocalDate.now();
        List<EmiSchedule> emis = emiRepo.findByStatus(EmiStatus.PENDING);

        for (EmiSchedule emi : emis) {
            LocalDate dueDate = emi.getDueDate();

            if (dueDate == null) {
                continue;
            }

            try {
                Integer customerId = accountRepo
                        .findById(emi.getLoanAccountId())
                        .map(account -> account.getCustomerId())
                        .orElseThrow(() ->
                                new EmiReminderException("Loan account not found"));

                Customer customer = customerRepo.findById(customerId)
                        .orElseThrow(() ->
                                new EmiReminderException("Customer not found"));

                String email = customer.getEmail();

                // 1. EMI overdue for more than 3 months
                if (today.isAfter(dueDate.plusMonths(3))) {

                    String title = "Overdue EMI Warning";

                    String message = "Your EMI of Rs. " + emi.getEmiAmount()
                            + " was due on " + dueDate
                            + " and remains unpaid for more than 3 months. "
                            + "Please contact our support team to discuss repayment. "
                            + "An authorised representative visit may be considered "
                            + "in accordance with applicable law and recovery policy.";

                    boolean created = notificationService.createOverdueWarning(
                            customerId,
                            title,
                            message,
                            emi.getEmiScheduleId());

                    if (created) {
                        if (email == null || email.isBlank()) {
                            log.warn("Customer email is missing for customer ID: {}",
                                    customerId);
                        } else {
                            emailService.sendEmail(
                                    email,
                                    "Important: Overdue EMI Payment Warning",
                                    "Dear Customer,\n\n"
                                            + message + "\n\n"
                                            + "Please contact our support team to discuss "
                                            + "your repayment options.\n\n"
                                            + "Regards,\nLoan Management Team");
                        }
                    }

                    continue;
                }

                /*// 2. Normal reminder: before due date or monthly while unpaid
                if (today.isBefore(dueDate.minusDays(3))) {
                    continue;
                }

                if (today.isAfter(dueDate)) {
                    // Send a reminder on the same day number every month
                    if (today.getDayOfMonth() != dueDate.getDayOfMonth()) {
                        continue;
                    }
                }*/

                // Testing: create reminder for pending EMIs whose due date has arrived
                if (today.isBefore(dueDate.minusDays(3))) {
                    continue;
                }

                String notificationMessage = "Your EMI of Rs. "
                        + emi.getEmiAmount()
                        + " is due on " + dueDate
                        + ". Please pay on time.";

                String emailMessage = "Dear Customer,\n\n"
                        + "This is a friendly reminder that your EMI payment "
                        + "of Rs. " + emi.getEmiAmount()
                        + " is due on " + dueDate + ".\n\n"
                        + "Please make your payment on or before the due date "
                        + "to avoid additional penalties.\n\n"
                        + "Regards,\nLoan Management Team";

                boolean created = notificationService.createEmiReminder(
                        customerId,
                        "EMI Payment Reminder",
                        notificationMessage,
                        emi.getEmiScheduleId());

                /*if (created) {
                    if (email == null || email.isBlank()) {
                        log.warn("Customer email is missing for customer ID: {}",
                                customerId);
                    } else {
                        emailService.sendEmail(
                                email,
                                "EMI Payment Reminder - Loan Management",
                                emailMessage);
                    }
                }*/

                if (created && email != null && !email.isBlank()) {
                    emailService.sendEmail(
                            email,
                            "EMI Payment Reminder - Loan Management",
                            emailMessage
                    );
                }

            } catch (RuntimeException ex) {
                log.error("Reminder failed for EMI ID: {}",
                        emi.getEmiScheduleId(), ex);
            }
        }
    }

}
