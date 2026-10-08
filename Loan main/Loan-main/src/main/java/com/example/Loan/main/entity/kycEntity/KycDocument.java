package com.example.Loan.main.entity.kycEntity;


import com.example.Loan.main.entity.cibilEntity.Customer;
import com.example.Loan.main.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "KycDocuments")
@Data
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DocumentId")
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Column(name = "DocumentType", nullable = false, length = 100)
    private String documentType;

    @Column(name = "FilePath", length = 1000)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "VerificationStatus", length = 50)
    private DocumentStatus verificationStatus;
}