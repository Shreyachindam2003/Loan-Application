package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.SanctionLetterResponse;

public interface SanctionLetterService {

    SanctionLetterResponse generate(Integer dealId);

    SanctionLetterResponse getById(Integer sanctionId);

    byte[] downloadPdf(Integer sanctionId);
}//dada