package com.example.Loan.main.dto.ticket;


import com.example.Loan.main.enums.ticket.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketResponse {

    private Integer ticketId;

    private Integer customerId;

    private Integer loanAccountId;

    private String subject;

    private String description;

    private TicketStatus status;

    private String attachmentPath;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<TicketResponseDto> responses;
}
