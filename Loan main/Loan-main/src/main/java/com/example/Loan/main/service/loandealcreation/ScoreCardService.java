package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.ScoreCardResponse;

public interface ScoreCardService {

    ScoreCardResponse generate(Integer customerId);

    ScoreCardResponse getById(Integer scoreCardId);
}//dada