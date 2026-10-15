package com.example.Loan.main.controller.ticket;

import com.example.Loan.main.dto.ticket.*;
import com.example.Loan.main.entity.ticket.TicketResponse;
import com.example.Loan.main.respository.ticket.TicketResponseRepository;
import com.example.Loan.main.service.ticket.SupportTicketService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;
    private final TicketResponseRepository ticketResponseRepository;

    public SupportTicketController(
            SupportTicketService supportTicketService,
            TicketResponseRepository ticketResponseRepository) {
        this.supportTicketService = supportTicketService;
        this.ticketResponseRepository = ticketResponseRepository;
    }

    // 1. Customer raises a support ticket with an optional attachment
    @PostMapping(
            value = "/support/tickets",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<SupportTicketResponse> createTicket(
            @ModelAttribute CreateSupportTicketRequest formRequest,
            @RequestPart(value = "ticket", required = false)
            CreateSupportTicketJsonRequest jsonRequest,
            @RequestPart(value = "attachment", required = false)
            MultipartFile attachment) {

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

        return ResponseEntity.ok(
                supportTicketService.createTicket(request)
        );
    }

    // 2. Create ticket using JSON without an attachment
    @PostMapping(
            value = "/support/tickets",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SupportTicketResponse> createTicketJson(
            @RequestBody CreateSupportTicketJsonRequest request) {

        return ResponseEntity.ok(
                supportTicketService.createTicketFromJson(request)
        );
    }

    // 3. Get one ticket with its responses and attachment path
    @GetMapping("/support/tickets/{ticketId}")
    public ResponseEntity<SupportTicketResponse> getTicketById(
            @PathVariable Integer ticketId) {

        return ResponseEntity.ok(
                supportTicketService.getTicketById(ticketId)
        );
    }

    // 4. Get all tickets belonging to a customer
    @GetMapping("/support/tickets/customer/{customerId}")
    public ResponseEntity<Page<SupportTicketResponse>> getTicketsByCustomer(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = createPageable(page, size);

        List<SupportTicketResponse> all =
                supportTicketService.getTicketsByCustomer(customerId);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());

        List<SupportTicketResponse> content =
                start >= all.size()
                        ? List.of()
                        : all.subList(start, end);

        return ResponseEntity.ok(
                new PageImpl<>(content, pageable, all.size())
        );
    }

    // 5. Admin/Loan Officer gets all tickets (paginated)
    @GetMapping("/admin/support/tickets")
    public ResponseEntity<Page<SupportTicketResponse>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = createPageable(page, size);

        List<SupportTicketResponse> all =
                supportTicketService.getAllTickets();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());

        List<SupportTicketResponse> content =
                start >= all.size()
                        ? List.of()
                        : all.subList(start, end);

        return ResponseEntity.ok(
                new PageImpl<>(content, pageable, all.size())
        );
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "page must be 0 or greater"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "size must be between 1 and 100"
            );
        }

        return PageRequest.of(page, size);
    }

    // 6. Loan Officer responds using JSON
    @PostMapping(
            value = "/admin/support/tickets/{ticketId}/responses",
            consumes = MediaType.APPLICATION_JSON_VALUE
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

    // 7. Loan Officer responds using multipart/form-data
    @PostMapping(
            value = "/admin/support/tickets/{ticketId}/responses",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<SupportTicketResponse> addAdminResponseMultipart(
            @PathVariable Integer ticketId,
            @ModelAttribute TicketResponseMultipartRequest formRequest,
            @RequestPart(value = "responseData", required = false)
            TicketResponseRequest jsonRequest,
            @RequestPart(value = "attachment", required = false)
            MultipartFile attachment) {

        TicketResponseMultipartRequest request = formRequest;

        if (jsonRequest != null) {
            request.setAdminId(jsonRequest.getAdminId());
            request.setResponse(jsonRequest.getResponse());
        }

        if (attachment != null && !attachment.isEmpty()) {
            request.setAttachment(attachment);
        }

        return ResponseEntity.ok(
                supportTicketService.addAdminResponseMultipart(
                        ticketId,
                        request
                )
        );
    }

    // 8. Open/download the attachment uploaded by the customer
    @GetMapping("/support/tickets/{ticketId}/attachment")
    public ResponseEntity<Resource> getTicketAttachment(
            @PathVariable Integer ticketId) {

        SupportTicketResponse ticket =
                supportTicketService.getTicketById(ticketId);

        String attachmentPath = ticket.getAttachmentPath();

        if (attachmentPath == null || attachmentPath.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No attachment found for this ticket"
            );
        }

        try {
            Path filePath = Paths.get(attachmentPath)
                    .toAbsolutePath()
                    .normalize();

            Path uploadDirectory = Paths.get("uploads", "tickets")
                    .toAbsolutePath()
                    .normalize();

            // Prevent paths outside the ticket upload directory
            if (!filePath.startsWith(uploadDirectory)
                    || !Files.isRegularFile(filePath)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment file not found"
                );
            }

            Resource resource = new UrlResource(filePath.toUri());

            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType =
                        MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\""
                                    + filePath.getFileName()
                                    .toString()
                                    .replace("\"", "")
                                    + "\""
                    )
                    .contentType(
                            MediaType.parseMediaType(contentType)
                    )
                    .body(resource);

        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not open attachment"
            );
        }
    }

// Download the customer's ticket attachment
    @GetMapping("/support/tickets/{ticketId}/attachment/download")
    public ResponseEntity<Resource> downloadTicketAttachment(
            @PathVariable Integer ticketId) {

        SupportTicketResponse ticket =
                supportTicketService.getTicketById(ticketId);

        String attachmentPath = ticket.getAttachmentPath();

        if (attachmentPath == null || attachmentPath.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No attachment found for this ticket"
            );
        }

        try {
            Path filePath = Paths.get(attachmentPath)
                    .toAbsolutePath()
                    .normalize();

            Path uploadDirectory = Paths.get("uploads", "tickets")
                    .toAbsolutePath()
                    .normalize();

            if (!filePath.startsWith(uploadDirectory)
                    || !Files.isRegularFile(filePath)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment file not found"
                );
            }

            Resource resource = new UrlResource(filePath.toUri());

            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + filePath.getFileName().toString()
                                    .replace("\"", "")
                                    + "\""
                    )
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not download attachment"
            );
        }
    }

    // View officer's response attachment in the browser
    @GetMapping("/support/tickets/{ticketId}/responses/{responseId}/attachment")
    public ResponseEntity<Resource> viewResponseAttachment(
            @PathVariable Integer ticketId,
            @PathVariable Integer responseId) {

        return getResponseAttachment(ticketId, responseId, false);
    }

    // Download officer's response attachment
    @GetMapping("/support/tickets/{ticketId}/responses/{responseId}/attachment/download")
    public ResponseEntity<Resource> downloadResponseAttachment(
            @PathVariable Integer ticketId,
            @PathVariable Integer responseId) {

        return getResponseAttachment(ticketId, responseId, true);
    }

    // Shared logic for viewing and downloading the response attachment
    private ResponseEntity<Resource> getResponseAttachment(
            Integer ticketId,
            Integer responseId,
            boolean download) {

        TicketResponse response = ticketResponseRepository.findById(responseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Response not found"
                ));

        // Make sure this response belongs to the requested ticket.
        if (!ticketId.equals(response.getTicketId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Response does not belong to this ticket"
            );
        }

        String attachmentPath = response.getAttachmentPath();

        if (attachmentPath == null || attachmentPath.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No attachment found for this response"
            );
        }

        try {
            Path filePath = Paths.get(attachmentPath)
                    .toAbsolutePath()
                    .normalize();

            Path uploadDirectory = Paths.get("uploads", "tickets")
                    .toAbsolutePath()
                    .normalize();

            if (!filePath.startsWith(uploadDirectory)
                    || !Files.isRegularFile(filePath)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Response attachment file not found"
                );
            }

            Resource resource = new UrlResource(filePath.toUri());

            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            String filename = filePath.getFileName()
                    .toString()
                    .replace("\"", "");

            String disposition = download
                    ? "attachment; filename=\"" + filename + "\""
                    : "inline; filename=\"" + filename + "\"";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not access response attachment",
                    exception
            );
        }
    }


    // Delete a customer's own support ticket
    @DeleteMapping("/support/tickets/{ticketId}")
    public ResponseEntity<Void> deleteCustomerTicket(
            @PathVariable Integer ticketId,
            @RequestParam Integer customerId) {

        supportTicketService.deleteCustomerTicket(ticketId, customerId);

        return ResponseEntity.noContent().build();
    }

}
