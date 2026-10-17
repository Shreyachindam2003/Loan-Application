package com.example.Loan.main.serviceImpl;

import com.example.Loan.main.entity.Customer;
import com.example.Loan.main.entity.CustomerNotification;
import com.example.Loan.main.entity.ForeClosureRequest;
import com.example.Loan.main.entity.LoanAccount;
import com.example.Loan.main.respository.CustomerNotificationRepository;
import com.example.Loan.main.respository.CustomerRepository;
import com.example.Loan.main.respository.ForeClosureRequestRepository;
import com.example.Loan.main.respository.LoanAccountRepository;
import com.example.Loan.main.service.ClosureNotificationService;
import com.example.Loan.main.service.EmailService;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClosureNotificationServiceImpl
        implements ClosureNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(ClosureNotificationServiceImpl.class);

    private final ForeClosureRequestRepository requestRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final CustomerRepository customerRepository;
    private final CustomerNotificationRepository notificationRepository;
    private final EmailService emailService;

    public ClosureNotificationServiceImpl(
            ForeClosureRequestRepository requestRepository,
            LoanAccountRepository loanAccountRepository,
            CustomerRepository customerRepository,
            CustomerNotificationRepository notificationRepository,
            EmailService emailService) {

        this.requestRepository = requestRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Override
    public void notifyClosureStatus(Integer requestId, String status) {

        ForeClosureRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Closure request not found: " + requestId));

        LoanAccount account = loanAccountRepository
                .findById(request.getLoanAccountId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Loan account not found: "
                                + request.getLoanAccountId()));

        if (account.getCustomerId() == null) {
            throw new EntityNotFoundException(
                    "Customer ID missing for loan account "
                            + account.getLoanAccountId());
        }

        Customer customer = customerRepository
                .findById(account.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Customer not found: " + account.getCustomerId()));

        String title = "Closure Request " + status;
        String message = "Your " + request.getForeClosureType()
                + " closure request #" + request.getRequestId()
                + " for loan account #" + request.getLoanAccountId()
                + " has been " + status.toLowerCase() + ".";

        CustomerNotification notification = new CustomerNotification();
        notification.setCustomerId(customer.getCustomerId());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);

        try {
            notificationRepository.save(notification);
        } catch (Exception ex) {
            log.error(
                    "Could not save in-app notification for closure request {}",
                    requestId,
                    ex
            );
        }

        try {
            emailService.sendClosureStatusEmail(
                    customer.getEmail(),
                    request.getRequestId(),
                    request.getLoanAccountId(),
                    request.getForeClosureType(),
                    status
            );
        } catch (Exception ex) {
            log.error(
                    "Could not send closure status email for request {}",
                    requestId,
                    ex
            );
        }
    }
}