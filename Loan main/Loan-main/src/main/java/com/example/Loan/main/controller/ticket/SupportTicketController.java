package com.example.Loan.main.controller.ticket;


import com.example.Loan.main.dto.ticket.*;
import com.example.Loan.main.service.ticket.SupportTicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(
            SupportTicketService supportTicketService) {

        this.supportTicketService = supportTicketService;
    }

    // 1. Customer raises a support ticket
    @PostMapping(
            value = "/support/tickets",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<SupportTicketResponse> createTicket(
            @ModelAttribute CreateSupportTicketRequest formRequest,
            @RequestPart(value = "ticket", required = false)
            CreateSupportTicketJsonRequest jsonRequest,
            @RequestPart(value = "attachment", required = false)
            MultipartFile attachment) {

        // Supports BOTH multipart styles:
        // 1) Flat form-data fields (customerId, subject, description, ...)
        // 2) A JSON part named "ticket" plus an optional file part named "attachment".
        // If a JSON part is supplied, use its values; otherwise keep the flat form fields.
        CreateSupportTicketRequest request = formRequest;
        if (jsonRequest != null) {
            request.setCustomerId(jsonRequest.getCustomerId());
            request.setLoanAccountId(jsonRequest.getLoanAccountId());
            request.setSubject(jsonRequest.getSubject());
            request.setDescription(jsonRequest.getDescription());
        }
        if (attachment != null && !attachment.isEmpty()) {
            request.setAttachment(attachment);
        }

        return ResponseEntity.ok(supportTicketService.createTicket(request));
    }

    // JSON request without attachment. Use multipart/form-data when uploading a file.
    @PostMapping(
            value = "/support/tickets",
            consumes = "application/json"
    )
    public ResponseEntity<SupportTicketResponse> createTicketJson(
            @RequestBody CreateSupportTicketJsonRequest request) {

        return ResponseEntity.ok(
                supportTicketService.createTicketFromJson(request)
        );
    }

    // 2. Get one ticket with responses
    @GetMapping("/support/tickets/{ticketId}")
    public ResponseEntity<SupportTicketResponse> getTicketById(
            @PathVariable Integer ticketId) {

        return ResponseEntity.ok(
                supportTicketService.getTicketById(ticketId)
        );
    }

    // 3. Get all tickets of a customer
    @GetMapping("/support/tickets/customer/{customerId}")
    public ResponseEntity<Page<SupportTicketResponse>> getTicketsByCustomer(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = createPageable(page, size);
        List<SupportTicketResponse> all = supportTicketService.getTicketsByCustomer(customerId);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<SupportTicketResponse> content =
                start >= all.size() ? List.of() : all.subList(start, end);
        return ResponseEntity.ok(new PageImpl<>(content, pageable, all.size()));
    }

    // 4. Admin/Loan Officer gets all support tickets (paginated).
    @GetMapping("/admin/support/tickets")
    public ResponseEntity<Page<SupportTicketResponse>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = createPageable(page, size);
        List<SupportTicketResponse> all = supportTicketService.getAllTickets();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<SupportTicketResponse> content =
                start >= all.size() ? List.of() : all.subList(start, end);
        return ResponseEntity.ok(new PageImpl<>(content, pageable, all.size()));
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be 0 or greater");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        return PageRequest.of(page, size);
    }

    // 5. Loan Officer responds to a ticket using JSON without an attachment.
    @PostMapping(
            value = "/admin/support/tickets/{ticketId}/responses",
            consumes = "application/json"
    )
    public ResponseEntity<SupportTicketResponse> addAdminResponseJson(
            @PathVariable Integer ticketId,
            @RequestBody TicketResponseRequest request) {

        return ResponseEntity.ok(
                supportTicketService.addAdminResponse(
                        ticketId,
                        request
                )
        );
    }

    // 6. Loan Officer responds using multipart/form-data.
    // attachment is optional.
    @PostMapping(
            value = "/admin/support/tickets/{ticketId}/responses",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<SupportTicketResponse> addAdminResponseMultipart(
            @PathVariable Integer ticketId,
            @ModelAttribute TicketResponseMultipartRequest formRequest,
            @RequestPart(value = "responseData", required = false)
            TicketResponseRequest jsonRequest,
            @RequestPart(value = "attachment", required = false)
            MultipartFile attachment) {

        // Supports flat form-data fields or a JSON part named "responseData",
        // with an optional attachment file.
        TicketResponseMultipartRequest request = formRequest;
        if (jsonRequest != null) {
            request.setAdminId(jsonRequest.getAdminId());
            request.setResponse(jsonRequest.getResponse());
        }
        if (attachment != null && !attachment.isEmpty()) {
            request.setAttachment(attachment);
        }

        return ResponseEntity.ok(
                supportTicketService.addAdminResponseMultipart(ticketId, request)
        );
    }
}
