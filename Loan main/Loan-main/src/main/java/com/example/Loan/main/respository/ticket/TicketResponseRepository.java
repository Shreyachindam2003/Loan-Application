        package com.example.Loan.main.respository.ticket;

import com.example.Loan.main.entity.ticket.TicketResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketResponseRepository
        extends JpaRepository<TicketResponse, Integer> {

    List<TicketResponse> findByTicketIdOrderByCreatedAtAsc(
            Integer ticketId
    );

    Optional<TicketResponse> findByResponseIdAndTicketId(
            Integer responseId,
            Integer ticketId
    );


    void deleteByTicketId(Integer ticketId);

}
