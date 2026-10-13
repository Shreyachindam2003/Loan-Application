package com.example.Loan.main.entity.ticket;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticketresponses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ResponseId")
    private Integer responseId;

    @Column(name = "TicketId", nullable = false)
    private Integer ticketId;

    @Column(name = "AdminId", nullable = false)
    private Integer adminId;

    @Column(name = "Response", nullable = false, length = 2000)
    private String response;

    @Column(name = "AttachmentPath", length = 1000)
    private String attachmentPath;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
