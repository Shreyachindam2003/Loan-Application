package com.example.Loan.main.dto.ticket;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class TicketResponseMultipartRequest {

    private Integer adminId;

    private String response;

    private MultipartFile attachment;
}
