package com.example.Loan.main.service.ticket;


import com.example.Loan.main.dto.ticket.*;

import java.util.List;

public interface SupportTicketService {

    SupportTicketResponse createTicket(CreateSupportTicketRequest request);

    SupportTicketResponse createTicketFromJson(CreateSupportTicketJsonRequest request);

    SupportTicketResponse getTicketById(Integer ticketId);

    List<SupportTicketResponse> getTicketsByCustomer(Integer customerId);

    List<SupportTicketResponse> getAllTickets();

    SupportTicketResponse addAdminResponse(
            Integer ticketId,
            TicketResponseRequest request
    );

    SupportTicketResponse addAdminResponseMultipart(
            Integer ticketId,
            TicketResponseMultipartRequest request
    );


    void deleteCustomerTicket(Integer ticketId, Integer customerId);

}
