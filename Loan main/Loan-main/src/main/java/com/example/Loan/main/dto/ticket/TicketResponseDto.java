package com.example.Loan.main.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDto {

    private Integer responseId;

    private Integer adminId;

    private String response;

    private String attachmentPath;

    private LocalDateTime createdAt;
}
