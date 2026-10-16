package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.SanctionLetterResponse;

public interface SanctionLetterService {

    SanctionLetterResponse generate(Integer dealId);

    SanctionLetterResponse getById(Integer sanctionId);

    SanctionLetterResponse getByDealId(Integer dealId);

    byte[] downloadPdf(Integer sanctionId);
}