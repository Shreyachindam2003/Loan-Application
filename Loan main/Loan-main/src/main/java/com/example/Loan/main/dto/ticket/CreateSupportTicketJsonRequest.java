package com.example.Loan.main.dto.ticket;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSupportTicketJsonRequest {
    private Integer customerId;
    private Integer loanAccountId;
    private String subject;
    private String description;
}
