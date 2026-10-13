package com.example.Loan.main.entity.ticket;


import com.example.Loan.main.enums.ticket.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "supporttickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TicketId")
    private Integer ticketId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "LoanAccountId")
    private Integer loanAccountId;

    @Column(name = "Subject", length = 255)
    private String subject;

    @Column(name = "Description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50)
    private TicketStatus status;

    @Column(name = "AttachmentPath", length = 1000)
    private String attachmentPath;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
