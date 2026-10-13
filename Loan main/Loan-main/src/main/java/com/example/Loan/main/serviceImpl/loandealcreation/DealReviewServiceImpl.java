package com.example.Loan.main.serviceImpl.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.DealDecisionRequest;
import com.example.Loan.main.dto.loandealcreation.response.LoanDealResponse;
import com.example.Loan.main.entity.loandealcreation.LoanDeal;
import com.example.Loan.main.exception.loandealcreation.BusinessException;
import com.example.Loan.main.exception.loandealcreation.ResourceNotFoundException;

import com.example.Loan.main.respository.loandealcreation.LoanDealRepository;
import com.example.Loan.main.service.loandealcreation.DealReviewService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealReviewServiceImpl implements DealReviewService {

    private final LoanDealRepository repo;
    private final JdbcTemplate jdbc;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public LoanDealResponse review(
            Integer dealId,
            Integer officerId,
            DealDecisionRequest request
    ) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM Users u
                JOIN Roles r ON r.RoleId = u.RoleId
                WHERE u.UserId = ?
                  AND r.RoleName = 'Loan Officer'
                """, Integer.class, officerId);

        if (count == null || count == 0) {
            throw new BusinessException(
                    "Selected user is not a loan officer"
            );
        }

        LoanDeal deal = repo.findByIdForUpdate(dealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan application not found"
                ));

        if (!"PENDING_REVIEW".equals(deal.getStatus())) {
            throw new BusinessException(
                    "Only pending applications can be reviewed"
            );
        }

        boolean approved =
                request.decision() == DealDecisionRequest.Decision.APPROVED;

        String remarks = request.remarks() == null
                ? ""
                : request.remarks().trim();

        if (!approved && remarks.isBlank()) {
            throw new BusinessException(
                    "Rejection reason is required"
            );
        }

        List<String> emails = jdbc.query(
                "SELECT Email FROM Customers WHERE CustomerId = ?",
                (rs, row) -> rs.getString("Email"),
                deal.getCustomerId()
        );

        if (emails.isEmpty()
                || emails.get(0) == null
                || emails.get(0).isBlank()) {

            throw new BusinessException("Customer email is missing");
        }

        String status = request.decision().name();

        deal.setStatus(status);
        deal.setApprovedAmount(
                approved ? deal.getLoanAmount() : null
        );

        repo.saveAndFlush(deal);

        jdbc.update("""
                INSERT INTO DealReviews
                    (DealId, OfficerId, Status, Remarks, ReviewedAt)
                VALUES (?, ?, ?, ?, ?)
                """,
                dealId,
                officerId,
                status,
                remarks,
                LocalDateTime.now()
        );

        String subject = approved
                ? "Your loan application is approved"
                : "Your loan application is rejected";

        String body = approved
                ? """
                  Your loan application has been approved.
                  Deal ID: %s
                  Approved amount: INR %s
                  Annual interest rate: %s%%
                  Tenure: %s months
                  EMI: INR %s

                  Sanction letter and disbursement are the next steps.
                  """.formatted(
                dealId,
                deal.getApprovedAmount(),
                deal.getInterestRate(),
                deal.getTenureMonths(),
                deal.getEmiAmount()
        )
                : """
                  Your loan application has been rejected.
                  Deal ID: %s
                  Reason: %s
                  """.formatted(dealId, remarks);

        jdbc.update("""
                INSERT INTO LoanDecisionEmails
                    (DealId, Recipient, Subject, Body)
                VALUES (?, ?, ?, ?)
                """,
                dealId,
                emails.get(0),
                subject,
                body
        );

        String notification = approved
                ? "Loan application " + dealId + " approved."
                : "Loan application " + dealId
                + " rejected. Reason: " + remarks;

        jdbc.update("""
                INSERT INTO Notifications
                    (CustomerId, Message, IsRead, CreatedAt)
                VALUES (?, ?, FALSE, CURRENT_TIMESTAMP)
                """,
                deal.getCustomerId(),
                notification
        );

        return modelMapper.map(deal, LoanDealResponse.class);
    }
}//dada