package com.example.Loan.main.service.KycService;



import com.example.Loan.main.dto.KycDto.KycDocumentRequest;
import com.example.Loan.main.dto.KycDto.KycDocumentResponse;
import com.example.Loan.main.dto.KycDto.KycReviewRequest;
import org.springframework.core.io.Resource;


import java.io.IOException;
import java.util.List;

public interface KycService {

    KycDocumentResponse uploadDocument(Long userId, KycDocumentRequest request) throws IOException;
    List<KycDocumentResponse> getCustomerDocuments(Long userId);
    KycDocumentResponse getCustomerDocument(Long userId, Long documentId);
    Resource viewCustomerDocument(Long userId, Long documentId);
    void deleteCustomerDocument(Long userId, Long documentId);
    List<KycDocumentResponse> getDocumentsForReview();
    KycDocumentResponse getDocumentForReview(Long documentId);
    Resource viewDocument(Long documentId);
    KycDocumentResponse reviewDocument(Long documentId, KycReviewRequest request);
}