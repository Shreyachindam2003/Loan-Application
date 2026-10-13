package com.example.Loan.main.dto.ticket;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateSupportTicketRequest {

    private Integer customerId;

    private Integer loanAccountId;

    private String subject;

    private String description;

    private MultipartFile attachment;
}