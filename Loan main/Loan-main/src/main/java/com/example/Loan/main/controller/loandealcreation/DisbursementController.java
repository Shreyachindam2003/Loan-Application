package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.DisbursementRequest;
import com.example.Loan.main.dto.loandealcreation.response.DisbursementResponse;
import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.service.loandealcreation.DisbursementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/disbursements")
@RequiredArgsConstructor
public class DisbursementController {
    //dada
    private final DisbursementService service;

    @PostMapping("/deals/{dealId}")
    public ResponseApi<DisbursementResponse> disburse(
            @PathVariable("dealId") Integer dealId,
            @Valid @RequestBody DisbursementRequest request
    ) {
        return ResponseApi.success(
                "Simulated disbursement completed; loan account ready",
                service.disburse(dealId, request)
        );
    }
}