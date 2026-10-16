package com.example.Loan.main.service;



import com.example.Loan.main.dto.KycDocumentRequest;
import com.example.Loan.main.dto.KycDocumentResponse;
import com.example.Loan.main.dto.KycReviewRequest;
import org.springframework.core.io.Resource;


import java.io.IOException;
import java.util.List;

public interface KycService {

    KycDocumentResponse uploadDocument(Integer userId, KycDocumentRequest request) throws IOException;
    List<KycDocumentResponse> getCustomerDocuments(Integer userId);
    KycDocumentResponse getCustomerDocument(Integer userId, Integer documentId);
    Resource viewCustomerDocument(Integer userId, Integer documentId);
    void deleteCustomerDocument(Integer userId, Integer documentId);
    List<KycDocumentResponse> getDocumentsForReview();
    KycDocumentResponse getDocumentForReview(Integer documentId);
    Resource viewDocument(Integer documentId);
    KycDocumentResponse reviewDocument(Integer documentId, KycReviewRequest request);
}