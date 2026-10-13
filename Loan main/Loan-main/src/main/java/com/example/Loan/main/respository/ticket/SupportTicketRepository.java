package com.example.Loan.main.respository.ticket;



import com.example.Loan.main.entity.ticket.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Integer> {

    List<SupportTicket> findByCustomerId(Integer customerId);
}
