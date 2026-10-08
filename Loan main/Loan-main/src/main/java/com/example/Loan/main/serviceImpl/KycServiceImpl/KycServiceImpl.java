package com.example.Loan.main.serviceImpl.KycServiceImpl;


import com.example.Loan.main.dto.KycDto.KycDocumentRequest;
import com.example.Loan.main.dto.KycDto.KycDocumentResponse;
import com.example.Loan.main.dto.KycDto.KycReviewRequest;
import com.example.Loan.main.entity.cibilEntity.Customer;
import com.example.Loan.main.entity.kycEntity.KycDocument;
import com.example.Loan.main.enums.DocumentStatus;
import com.example.Loan.main.exception.cibilExceptions.KycCustomerNotFoundException;
import com.example.Loan.main.exception.cibilExceptions.KycDocumentAlreadyApprovedException;
import com.example.Loan.main.exception.cibilExceptions.KycDocumentNotFoundException;
import com.example.Loan.main.exception.cibilExceptions.KycFileException;
import com.example.Loan.main.respository.KycRepository.KycDocumentRepository;
import com.example.Loan.main.respository.cibilRespository.customerRepository;
import com.example.Loan.main.service.KycService.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycServiceImpl implements KycService {

    private final ModelMapper modelMapper;
    private final KycDocumentRepository kycDocumentRepository;
    private final customerRepository customerRepository;
    //  private final NotificationService notificationService;
    // private final EmailService emailService;
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new KycFileException("Please select a file");
        }

        if (file.getOriginalFilename() == null) {
            throw new KycFileException("Invalid file name");
        }
    }



    @Override
    public KycDocumentResponse uploadDocument(
            Long userId, KycDocumentRequest request) throws IOException {

        MultipartFile file = request.getFile();

        log.info("KYC document upload started, documentType={}",
                request.getDocumentType());

        validateFile(file);

        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "kyc");

        Files.createDirectories(uploadDir);

        String fileName = UUID.randomUUID() + "_" + "PAN" + file.getOriginalFilename() ;

        Path filePath = uploadDir.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.debug("KYC file saved successfully, fileName={}", fileName);

        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new KycCustomerNotFoundException("Customer not found"));
        KycDocument document = modelMapper.map(request, KycDocument.class);

        document.setCustomer(customer);
        document.setDocumentType(request.getDocumentType());
        document.setFilePath(filePath.toString());
        document.setVerificationStatus(DocumentStatus.PENDING);

        KycDocument savedDocument = kycDocumentRepository.save(document);

        log.info(
                "KYC document saved successfully, documentId={}, userId={}, status={}",
                savedDocument.getDocumentId(),
                customer.getCustomerId(),
                savedDocument.getVerificationStatus()
        );

//        Customer officer = customerRepository.findByRole(Role.OFFICER)
//                .orElseThrow(() ->
//                        new RuntimeException("Officer not found"));
//
//        notificationService.sendNotification(
//                officer.getUserId(),
//                NotificationType.KYC_DOCUMENT_UPLOADED,
//                "New KYC Document",
//                "A new " + savedDocument.getDocumentType()
//                        + " document is waiting for verification.",
//                savedDocument.getDocumentId()
//        );

        log.info(
                "KYC upload notification sent, officerId={}, documentId={}",
                //    officer.getUserId(),
                savedDocument.getDocumentId()
        );

        KycDocumentResponse response = modelMapper.map(savedDocument, KycDocumentResponse.class);

        response.setCustomerId(savedDocument.getCustomer().getCustomerId());

        return response;
    }


    @Override
    public List<KycDocumentResponse> getCustomerDocuments(Long userId) {

        List<KycDocument> documents =
                kycDocumentRepository.findByCustomerCustomerId(userId);

        return documents.stream()
                .map(document -> {

                    KycDocumentResponse response = modelMapper.map(document, KycDocumentResponse.class);

                    response.setCustomerId(document.getCustomer().getCustomerId());
                    return response;
                })
                .collect(Collectors.toList());
    }


    @Override
    public KycDocumentResponse getCustomerDocument(
            Long userId,
            Long documentId) {

        KycDocument document =
                kycDocumentRepository.findByDocumentIdAndCustomerCustomerId(documentId, userId)
                        .orElseThrow(() ->
                                new KycDocumentNotFoundException  ("KYC document not found"));

        KycDocumentResponse response =
                modelMapper.map(
                        document,
                        KycDocumentResponse.class
                );

        response.setCustomerId(
                document.getCustomer().getCustomerId()
        );

        return response;
    }


    @Override
    public Resource viewCustomerDocument(
            Long userId,
            Long documentId) {

        KycDocument document =
                kycDocumentRepository
                        .findByDocumentIdAndCustomerCustomerId(
                                documentId,
                                userId
                        )
                        .orElseThrow(() ->
                                new KycDocumentNotFoundException (
                                        "KYC document not found"
                                ));

        if (document.getFilePath() == null) {
            throw new KycFileException ("File path not found");
        }

        Path path = Paths.get(document.getFilePath());

        if (!Files.exists(path)) {
            throw new KycFileException("Document file not found");
        }

        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new KycFileException(
                    "Unable to load KYC document",
                    e
            );
        }
    }

    @Override
    public void deleteCustomerDocument(
            Long userId,
            Long documentId) {

        KycDocument document =
                kycDocumentRepository
                        .findByDocumentIdAndCustomerCustomerId(
                                documentId,
                                userId
                        )
                        .orElseThrow(() ->
                                new KycDocumentNotFoundException(
                                        "KYC document not found"
                                ));

        if (document.getFilePath() != null) {

            Path path =
                    Paths.get(document.getFilePath());

            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new KycFileException(
                        "Unable to delete KYC file",
                        e
                );
            }
        }

        kycDocumentRepository.delete(document);
    }

    @Override
    public Resource viewDocument(Long documentId) {

        KycDocument document =
                kycDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new KycDocumentNotFoundException("KYC document not found"));

        Path path = Paths.get(document.getFilePath());

        if (!Files.exists(path)) {
            throw new KycFileException("Document file not found");
        }

        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new KycFileException("Unable to load document", e);
        }
    }

    @Override
    public List<KycDocumentResponse> getDocumentsForReview() {

        List<KycDocument> documents =
                kycDocumentRepository.findByVerificationStatus(
                        DocumentStatus.PENDING
                );

        return documents.stream()
                .map(document -> {

                    KycDocumentResponse response =
                            modelMapper.map(
                                    document,
                                    KycDocumentResponse.class
                            );

                    response.setCustomerId(
                            document.getCustomer().getCustomerId()
                    );

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public KycDocumentResponse getDocumentForReview(Long documentId) {

        KycDocument document =
                kycDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new KycDocumentNotFoundException(
                                        "KYC document not found"
                                ));

        KycDocumentResponse response =
                modelMapper.map(
                        document,
                        KycDocumentResponse.class
                );

        response.setCustomerId(
                document.getCustomer().getCustomerId()
        );

        return response;
    }
    @Override
    public KycDocumentResponse reviewDocument(
            Long documentId,
            KycReviewRequest request) {

        log.info(
                "KYC document review started, documentId={}, requestedStatus={}",
                documentId,
                request.getStatus()
        );

        KycDocument document =
                kycDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new KycDocumentNotFoundException(
                                        "KYC document not found"
                                ));

        if (document.getVerificationStatus() == DocumentStatus.APPROVED) {

            log.warn(
                    "Attempt to modify already approved KYC, documentId={}",
                    documentId
            );

            throw new KycDocumentAlreadyApprovedException(
                    "Approved KYC document cannot be changed"
            );
        }

        if (request.getStatus() == DocumentStatus.REJECTED
                && (request.getRejectionReason() == null
                || request.getRejectionReason().isBlank())) {

            log.warn(
                    "KYC rejection attempted without reason, documentId={}",
                    documentId
            );

            throw new RuntimeException(
                    "Rejection reason is required"
            );
        }

        DocumentStatus oldStatus =
                document.getVerificationStatus();

        document.setVerificationStatus(request.getStatus());

        KycDocument updatedDocument =
                kycDocumentRepository.save(document);

        log.info(
                "KYC document status updated, documentId={}, oldStatus={}, newStatus={}",
                documentId,
                oldStatus,
                updatedDocument.getVerificationStatus()
        );

//        Customer customer = updatedDocument.getCustomer();
//
//        // APPROVED
//        if (request.getStatus() == DocumentStatus.APPROVED) {
//
//            notificationService.sendNotification(
//                    customer.getUserId(),
//                    NotificationType.KYC_DOCUMENT_APPROVED,
//                    "KYC Document Approved",
//                    "Your " + updatedDocument.getDocumentType()
//                            + " document has been approved.",
//                    updatedDocument.getDocumentId()
//            );
//            emailService.sendKycStatusEmail(
//                    customer.getEmail(),
//                    customer.getFirstName(),
//                    updatedDocument.getDocumentType(),
//                    DocumentStatus.APPROVED,
//                    null
//            );
//
//            log.info(
//                    "KYC approval notification and email triggered, documentId={}, customerId={}",
//                    documentId,
//                    customer.getCustomerId()
//            );
//        }
//
//        // REJECTED
//        if (request.getStatus() == DocumentStatus.REJECTED) {
//
//            String message =
//                    "Your " + updatedDocument.getDocumentType()
//                            + " document has been rejected.";
//
//            if (request.getRejectionReason() != null
//                    && !request.getRejectionReason().isBlank()) {
//
//                message += " Reason: "
//                        + request.getRejectionReason();
//            }
//
//            notificationService.sendNotification(
//                    customer.getUserId(),
//                    NotificationType.KYC_DOCUMENT_REJECTED,
//                    "KYC Document Rejected",
//                    message,
//                    updatedDocument.getDocumentId()
//            );
//            emailService.sendKycStatusEmail(
//                    customer.getEmail(),
//                    customer.getFirstName(),
//                    updatedDocument.getDocumentType(),
//                    DocumentStatus.REJECTED,
//                    request.getRejectionReason()
//            );
//
//            log.info(
//                    "KYC rejection notification and email triggered, documentId={}, customerId={}",
//                    documentId,
//                    customer.getCustomerId()
//            );
//
//        }


        KycDocumentResponse response =
                modelMapper.map(
                        updatedDocument,
                        KycDocumentResponse.class
                );

        response.setCustomerId(
                updatedDocument.getCustomer().getCustomerId()
        );

        return response;
    }
}