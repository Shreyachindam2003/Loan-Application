package com.example.Loan.main.controller;

import java.util.List;

import com.example.Loan.main.dto.ClosureResponseDto;
import com.example.Loan.main.dto.ForeClosureRequestDto;
import com.example.Loan.main.dto.LoanClosureDto;
import com.example.Loan.main.entity.ForeClosureRequest;
import com.example.Loan.main.entity.LoanClosure;
import com.example.Loan.main.service.ClosureNotificationService;
import com.example.Loan.main.service.ClosureService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/closure")
@RequiredArgsConstructor
public class ClosureController {

    private final ClosureService closureService;
    private final ClosureNotificationService notificationService;

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.CREATED)
    public ClosureResponseDto createRequest(
            @Valid @RequestBody ForeClosureRequestDto dto) {

        return closureService.createRequest(dto);
    }

    @GetMapping("/requests")
    public List<ForeClosureRequest> getRequests() {
        return closureService.getRequests();
    }

    @PostMapping("/complete")
    public LoanClosure completeClosure(
            @Valid @RequestBody LoanClosureDto dto) {

        return closureService.createClosure(dto);
    }

    @PatchMapping("/requests/{requestId}/approve")
    public ClosureResponseDto approveRequest(
            @PathVariable Integer requestId) {

        ClosureResponseDto result =
                closureService.approveRequest(requestId);

        notificationService.notifyClosureStatus(requestId, "APPROVED");

        return result;
    }

    @PatchMapping("/requests/{requestId}/reject")
    public ClosureResponseDto rejectRequest(
            @PathVariable Integer requestId) {

        ClosureResponseDto result =
                closureService.rejectRequest(requestId);

        notificationService.notifyClosureStatus(requestId, "REJECTED");

        return result;
    }
}