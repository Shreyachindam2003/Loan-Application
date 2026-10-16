package com.example.Loan.main.serviceImpl;


import com.example.Loan.main.dto.KycDocumentRequest;
import com.example.Loan.main.dto.KycDocumentResponse;
import com.example.Loan.main.dto.KycReviewRequest;
import com.example.Loan.main.entity.Customer;
import com.example.Loan.main.entity.KycDocument;
import com.example.Loan.main.enums.DocumentStatus;
import com.example.Loan.main.enums.NotificationType;
import com.example.Loan.main.exception.KycCustomerNotFoundException;
import com.example.Loan.main.exception.KycDocumentAlreadyApprovedException;
import com.example.Loan.main.exception.KycDocumentNotFoundException;
import com.example.Loan.main.exception.KycFileException;
import com.example.Loan.main.respository.KycDocumentRepository;
import com.example.Loan.main.respository.UserRepository;
import com.example.Loan.main.respository.customerRepository;
import com.example.Loan.main.service.KycService;
import com.example.Loan.main.service.NotificationService;
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

private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final KycDocumentRepository kycDocumentRepository;
    private final customerRepository customerRepository;
    private final UserRepository userRepository;
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
            Integer userId, KycDocumentRequest request) throws IOException {
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

        Customer customer1 = customerRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Officer not found"));

        notificationService.createNotification(
                customer1.getCustomerId(),
                "New KYC Document",
                "A new " + savedDocument.getDocumentType()
                        + " document is waiting for verification.",
                NotificationType.KYC_DOCUMENT_UPLOADED,
               savedDocument.getDocumentId()
        );

        log.info(
                "KYC upload notification created, officerId={}, documentId={}",
                customer1.getCustomerId(),
                savedDocument.getDocumentId()
        );

        KycDocumentResponse response = new KycDocumentResponse();

        Customer savedCustomer = savedDocument.getCustomer();

        response.setDocumentId(savedDocument.getDocumentId());
        response.setCustomerId(savedCustomer.getCustomerId());
        response.setDocumentType(savedDocument.getDocumentType());
        response.setFilePath(savedDocument.getFilePath());
        response.setVerificationStatus(savedDocument.getVerificationStatus());

        response.setCustomerName(
                savedCustomer.getFirstName() + " " + savedCustomer.getLastName()
        );
        response.setEmail(savedCustomer.getEmail());
        response.setMobile(savedCustomer.getMobileNo());

        return response;


    }


    @Override
    public List<KycDocumentResponse> getCustomerDocuments(Integer userId) {

        List<KycDocument> documents =
                kycDocumentRepository.findByCustomerCustomerId(userId);

        return documents.stream()
                .map(document -> {

                    KycDocumentResponse response = new KycDocumentResponse();

                    response.setDocumentId(document.getDocumentId());

                    response.setCustomerId(
                            document.getCustomer().getCustomerId()
                    );

                    response.setCustomerName(
                            document.getCustomer().getFirstName() + " " +
                                    document.getCustomer().getLastName()
                    );

                    response.setEmail(
                            document.getCustomer().getEmail()
                    );

                    response.setMobile(
                            document.getCustomer().getMobileNo()
                    );

                    response.setDocumentType(document.getDocumentType());

                    response.setFilePath(document.getFilePath());

                    response.setVerificationStatus(
                            document.getVerificationStatus()
                    );

                    return response;
                })
                .collect(Collectors.toList());
    }


    @Override
    public KycDocumentResponse getCustomerDocument(
            Integer userId,
            Integer documentId) {

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
            Integer userId,
            Integer documentId) {

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
            Integer userId,
            Integer documentId) {

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
    public Resource viewDocument(Integer documentId) {

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
                            new KycDocumentResponse();

                    Customer customer = document.getCustomer();

                    response.setDocumentId(document.getDocumentId());
                    response.setCustomerId(customer.getCustomerId());
                    response.setDocumentType(document.getDocumentType());
                    response.setFilePath(document.getFilePath());
                    response.setVerificationStatus(
                            document.getVerificationStatus()
                    );

                    response.setCustomerName(
                            customer.getFirstName() + " "
                                    + customer.getLastName()
                    );
                    response.setEmail(customer.getEmail());
                    response.setMobile(customer.getMobileNo());

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public KycDocumentResponse getDocumentForReview(Integer documentId) {

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
            Integer documentId,
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

        Customer customer1 = updatedDocument.getCustomer();

        if (request.getStatus() == DocumentStatus.APPROVED) {

            notificationService.createNotification(
                   customer1.getCustomerId(),
                    "KYC Document Approved",
                    "Your " + updatedDocument.getDocumentType()
                            + " document has been approved.",
                    NotificationType.KYC_DOCUMENT_APPROVED,
                    updatedDocument.getDocumentId()
            );


            log.info(
                    "KYC approval notification and email triggered, documentId={}, customerId={}",
                    documentId,
                    customer1.getCustomerId()
            );
        }

        if (request.getStatus() == DocumentStatus.REJECTED) {

            String message =
                    "Your " + updatedDocument.getDocumentType()
                            + " document has been rejected.";

            if (request.getRejectionReason() != null
                    && !request.getRejectionReason().isBlank()) {

                message += " Reason: "
                        + request.getRejectionReason();
            }
            notificationService.createNotification(
                    customer1.getCustomerId(),
                    "KYC Document Rejected",
                    message,
                    NotificationType.KYC_DOCUMENT_REJECTED,
                   updatedDocument.getDocumentId()
            );

            log.info(
                    "KYC rejection notification and email triggered, documentId={}, customerId={}",
                    documentId,
                    customer1.getCustomerId()
            );

        }


        KycDocumentResponse response = new KycDocumentResponse();

        Customer customer = updatedDocument.getCustomer();

        response.setDocumentId(updatedDocument.getDocumentId());
        response.setCustomerId(customer.getCustomerId());
        response.setDocumentType(updatedDocument.getDocumentType());
        response.setFilePath(updatedDocument.getFilePath());
        response.setVerificationStatus(updatedDocument.getVerificationStatus());

        response.setCustomerName(
                customer.getFirstName() + " " + customer.getLastName()
        );
        response.setEmail(customer.getEmail());
        response.setMobile(customer.getMobileNo());

        return response;

    }
}