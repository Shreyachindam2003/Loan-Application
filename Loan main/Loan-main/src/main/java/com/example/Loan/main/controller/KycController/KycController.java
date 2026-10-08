package com.example.Loan.main.controller.KycController;


import com.example.Loan.main.dto.KycDto.KycDocumentRequest;
import com.example.Loan.main.dto.KycDto.KycDocumentResponse;
import com.example.Loan.main.dto.KycDto.KycReviewRequest;
import com.example.Loan.main.dto.cibilDto.ApiResponse;
import com.example.Loan.main.service.KycService.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;



    @PostMapping(
            value = "/customers/{userId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<KycDocumentResponse>> uploadDocument(
            @PathVariable Long userId,
            @Valid @ModelAttribute KycDocumentRequest request
    ) throws IOException {

        KycDocumentResponse response =
                kycService.uploadDocument(userId, request);

        return ResponseEntity.status(201).body(
                new ApiResponse<>(
                        true,
                        "Document uploaded successfully",
                        response
                )
        );
    }


    @GetMapping("/customers/{userId}/documents")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> getCustomerDocuments(
            @PathVariable Long userId
    ) {

        List<KycDocumentResponse> documents =
                kycService.getCustomerDocuments(userId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Documents fetched successfully",
                        documents
                )
        );
    }


    @GetMapping("/customers/{userId}/documents/{documentId}")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> getCustomerDocument(
            @PathVariable Long userId,
            @PathVariable Long documentId
    ) {

        KycDocumentResponse response =
                kycService.getCustomerDocument(
                        userId,
                        documentId
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document fetched successfully",
                        response
                )
        );
    }


    @GetMapping("/customers/{userId}/documents/{documentId}/view")
    public ResponseEntity<Resource> viewCustomerDocument(
            @PathVariable Long userId,
            @PathVariable Long documentId
    ) {

        Resource resource =
                kycService.viewCustomerDocument(
                        userId,
                        documentId
                );

        return buildFileResponse(resource);
    }


    @DeleteMapping("/customers/{userId}/documents/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomerDocument(
            @PathVariable Long userId,
            @PathVariable Long documentId
    ) {

        kycService.deleteCustomerDocument(
                userId,
                documentId
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document deleted successfully",
                        null
                )
        );
    }



    @GetMapping("/officer/pending")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>>
    getDocumentsForReview() {

        List<KycDocumentResponse> documents =
                kycService.getDocumentsForReview();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Pending documents fetched successfully",
                        documents
                )
        );
    }


    @GetMapping("/officer/documents/{documentId}")
    public ResponseEntity<ApiResponse<KycDocumentResponse>>
    getDocumentForReview(
            @PathVariable Long documentId
    ) {

        KycDocumentResponse response =
                kycService.getDocumentForReview(documentId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document fetched successfully",
                        response
                )
        );
    }


    @GetMapping("/officer/documents/{documentId}/view")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long documentId
    ) {

        Resource resource =
                kycService.viewDocument(documentId);

        return buildFileResponse(resource);
    }


    @PutMapping("/officer/documents/{documentId}/review")
    public ResponseEntity<ApiResponse<KycDocumentResponse>>
    reviewDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody KycReviewRequest request
    ) {

        KycDocumentResponse response =
                kycService.reviewDocument(
                        documentId,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document reviewed successfully",
                        response
                )
        );
    }


    // =====================================================
    // COMMON FILE RESPONSE
    // =====================================================

    private ResponseEntity<Resource> buildFileResponse(
            Resource resource
    ) {

        MediaType mediaType =
                MediaTypeFactory.getMediaType(
                        resource.getFilename()
                ).orElse(
                        MediaType.APPLICATION_OCTET_STREAM
                );

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                resource.getFilename() +
                                "\""
                )
                .body(resource);
    }
}