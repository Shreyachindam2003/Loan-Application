package com.example.Loan.main.respository.KycRepository;

import com.example.Loan.main.entity.kycEntity.KycDocument;
import com.example.Loan.main.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    List<KycDocument> findByCustomerCustomerId(Long customerId);
    Optional<KycDocument> findByDocumentIdAndCustomerCustomerId(Long documentId, Long customerId);
    List<KycDocument> findByVerificationStatus(DocumentStatus verificationStatus);
}