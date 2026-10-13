package com.example.Loan.main.respository.ticket;


import com.example.Loan.main.entity.ticket.TicketResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketResponseRepository extends JpaRepository<TicketResponse, Integer> {

    List<TicketResponse> findByTicketIdOrderByCreatedAtAsc(Integer ticketId);
}
