package com.example.Loan.main.serviceImpl.ticket;

import com.example.Loan.main.dto.ticket.*;
import com.example.Loan.main.entity.ticket.SupportTicket;
import com.example.Loan.main.entity.ticket.TicketResponse;
import com.example.Loan.main.entity.ticketnotification.Notification;
import com.example.Loan.main.enums.ticket.TicketStatus;
import com.example.Loan.main.enums.ticketnotification.NotificationType;
import com.example.Loan.main.exception.ResourceNotFoundException;
import com.example.Loan.main.respository.ticket.SupportTicketRepository;
import com.example.Loan.main.respository.ticket.TicketResponseRepository;
import com.example.Loan.main.respository.ticketnotification.NotificationRepository;
import com.example.Loan.main.service.ticket.SupportTicketService;
import com.example.Loan.main.util.ticket.FileStorageUtil;
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final TicketResponseRepository ticketResponseRepository;
    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;
    private final JdbcTemplate jdbcTemplate;

    public SupportTicketServiceImpl(
            SupportTicketRepository supportTicketRepository,
            TicketResponseRepository ticketResponseRepository,
            NotificationRepository notificationRepository,
            ModelMapper modelMapper,
            JdbcTemplate jdbcTemplate) {
        this.supportTicketRepository = supportTicketRepository;
        this.ticketResponseRepository = ticketResponseRepository;
        this.notificationRepository = notificationRepository;
        this.modelMapper = modelMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public SupportTicketResponse createTicket(CreateSupportTicketRequest request) {
        try {
            // Always create a fresh entity for a new ticket. Do not let DTO mapping
            // accidentally populate a generated primary key and make JPA merge it.
            SupportTicket ticket = new SupportTicket();
            ticket.setCustomerId(request.getCustomerId());
            ticket.setLoanAccountId(request.getLoanAccountId());
            ticket.setSubject(request.getSubject());
            ticket.setDescription(request.getDescription());
            ticket.setStatus(TicketStatus.OPEN);

            String attachmentPath = FileStorageUtil.saveFile(request.getAttachment());
            ticket.setAttachmentPath(attachmentPath);

            LocalDateTime now = LocalDateTime.now();
            ticket.setCreatedAt(now);
            ticket.setUpdatedAt(now);

            SupportTicket savedTicket = supportTicketRepository.save(ticket);

            // Notify a Loan Officer when a new ticket is raised.
            Integer loanOfficerUserId = findLoanOfficerUserId();
            if (loanOfficerUserId != null) {
                Notification notification = new Notification();
                notification.setUserId(loanOfficerUserId);
                notification.setMessage(
                        "New support ticket #" + savedTicket.getTicketId()
                                + " has been raised by customer #" + savedTicket.getCustomerId()
                );
                notification.setNotificationType(NotificationType.NEW_TICKET);
                notification.setIsRead(false);
                notification.setCreatedAt(now);
                notificationRepository.save(notification);
            }

            return toResponse(savedTicket);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save ticket attachment", e);
        }
    }

    @Override
    @Transactional
    public SupportTicketResponse createTicketFromJson(CreateSupportTicketJsonRequest request) {
        SupportTicket ticket = new SupportTicket();
        ticket.setCustomerId(request.getCustomerId());
        ticket.setLoanAccountId(request.getLoanAccountId());
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setAttachmentPath(null);

        LocalDateTime now = LocalDateTime.now();
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        Integer loanOfficerUserId = findLoanOfficerUserId();
        if (loanOfficerUserId != null) {
            Notification notification = new Notification();
            notification.setUserId(loanOfficerUserId);
            notification.setMessage("New support ticket #" + savedTicket.getTicketId()
                    + " has been raised by customer #" + savedTicket.getCustomerId());
            notification.setNotificationType(NotificationType.NEW_TICKET);
            notification.setIsRead(false);
            notification.setCreatedAt(now);
            notificationRepository.save(notification);
        }
        return toResponse(savedTicket);
    }

    @Override
    public SupportTicketResponse getTicketById(Integer ticketId) {
        SupportTicket ticket = getTicket(ticketId);
        return toResponse(ticket);
    }

    @Override
    public List<SupportTicketResponse> getTicketsByCustomer(Integer customerId) {
        return supportTicketRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<SupportTicketResponse> getAllTickets() {
        return supportTicketRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SupportTicketResponse addAdminResponse(
            Integer ticketId,
            TicketResponseRequest request) {

        SupportTicket ticket = getTicket(ticketId);
        TicketResponse ticketResponse = new TicketResponse();
        ticketResponse.setTicketId(ticketId);
        ticketResponse.setAdminId(request.getAdminId());
        ticketResponse.setResponse(request.getResponse());
        ticketResponse.setAttachmentPath(null);
        ticketResponse.setCreatedAt(LocalDateTime.now());
        ticketResponseRepository.save(ticketResponse);

        closeTicketAndNotifyCustomer(ticket);
        return getTicketById(ticketId);
    }

    @Override
    @Transactional
    public SupportTicketResponse addAdminResponseMultipart(
            Integer ticketId,
            TicketResponseMultipartRequest request) {

        SupportTicket ticket = getTicket(ticketId);

        try {
            TicketResponse ticketResponse = new TicketResponse();
            ticketResponse.setTicketId(ticketId);
            ticketResponse.setAdminId(request.getAdminId());
            ticketResponse.setResponse(request.getResponse());
            ticketResponse.setAttachmentPath(
                    FileStorageUtil.saveFile(request.getAttachment())
            );
            ticketResponse.setCreatedAt(LocalDateTime.now());
            ticketResponseRepository.save(ticketResponse);

            closeTicketAndNotifyCustomer(ticket);
            return getTicketById(ticketId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save response attachment", e);
        }
    }

    private void closeTicketAndNotifyCustomer(SupportTicket ticket) {
        // A response means the issue has been resolved as per the current requirement.
        LocalDateTime now = LocalDateTime.now();
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setUpdatedAt(now);
        supportTicketRepository.save(ticket);

        // Notify the customer who raised the ticket.
        Notification notification = new Notification();
        notification.setCustomerId(ticket.getCustomerId());
        notification.setMessage(
                "Your support ticket #" + ticket.getTicketId()
                        + " has been resolved. Please check the response."
        );
        notification.setNotificationType(NotificationType.TICKET_RESOLVED);
        notification.setIsRead(false);
        notification.setCreatedAt(now);
        notificationRepository.save(notification);
    }

    private SupportTicket getTicket(Integer ticketId) {
        return supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + ticketId));
    }

    private SupportTicketResponse toResponse(SupportTicket ticket) {
        SupportTicketResponse response =
                modelMapper.map(ticket, SupportTicketResponse.class);

        List<TicketResponseDto> responseDtos =
                ticketResponseRepository
                        .findByTicketIdOrderByCreatedAtAsc(ticket.getTicketId())
                        .stream()
                        .map(item -> modelMapper.map(item, TicketResponseDto.class))
                        .toList();

        response.setResponses(responseDtos);
        return response;
    }

    /**
     * Uses the existing Users/Roles tables from the supplied schema.
     * No new User/Role JPA entities are introduced into the team project.
     */
    private Integer findLoanOfficerUserId() {
        List<Integer> ids = jdbcTemplate.query(
                """
                SELECT u.UserId
                FROM Users u
                JOIN Roles r ON r.RoleId = u.RoleId
                WHERE r.RoleName = 'Loan Officer'
                ORDER BY u.UserId
                LIMIT 1
                """,
                (rs, rowNum) -> rs.getInt("UserId")
        );

        return ids.isEmpty() ? null : ids.get(0);
    }
}
