package com.example.Loan.main.dto.KycDto;


import com.example.Loan.main.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycDocumentResponse {

    private Long documentId;
    private Long customerId;
    private String documentType;
    private String filePath;
    private DocumentStatus verificationStatus;

}
