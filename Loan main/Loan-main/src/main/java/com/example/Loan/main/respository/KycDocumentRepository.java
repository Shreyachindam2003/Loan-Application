package com.example.Loan.main.respository;

import com.example.Loan.main.entity.KycDocument;
import com.example.Loan.main.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Integer> {
    List<KycDocument> findByCustomerCustomerId(Integer customerId);
    Optional<KycDocument> findByDocumentIdAndCustomerCustomerId(Integer documentId, Integer customerId);
    List<KycDocument> findByVerificationStatus(DocumentStatus verificationStatus);
}