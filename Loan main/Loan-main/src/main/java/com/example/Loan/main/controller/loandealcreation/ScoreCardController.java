package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.dto.loandealcreation.response.ScoreCardResponse;
import com.example.Loan.main.service.loandealcreation.ScoreCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scorecards")
@RequiredArgsConstructor
public class ScoreCardController {

    private final ScoreCardService service;

    @PostMapping("/customers/{customerId}")
    public ResponseApi<ScoreCardResponse> generate(
            @PathVariable("customerId") Integer customerId  
    ) {
        return ResponseApi.success(
                "Scorecard ready",
                service.generate(customerId)
        );
    }

    @GetMapping("/{scoreCardId}")
    public ResponseApi<ScoreCardResponse> getById(
            @PathVariable("scoreCardId") Integer scoreCardId
    ) {
        return ResponseApi.success(
                "Scorecard fetched successfully",
                service.getById(scoreCardId)
        );
    }

    @GetMapping("/customer/{customerId}/latest")
    public ResponseApi<ScoreCardResponse> getLatest(
            @PathVariable("customerId") Integer customerId
    ) {
        return ResponseApi.success(
                "Latest customer scorecard fetched",
                service.getLatestByCustomerId(customerId)
        );
    }
}