package com.example.loanapplication.controller.closure;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.loanapplication.dto.closure.ClosureResponseDto;
import com.example.loanapplication.dto.closure.ForeClosureRequestDto;
import com.example.loanapplication.dto.closure.LoanClosureDto;
import com.example.loanapplication.dto.closure.LoanPaymentDto;
import com.example.loanapplication.entity.closure.ForeClosureRequest;
import com.example.loanapplication.entity.closure.LoanClosure;
import com.example.loanapplication.entity.closure.LoanPayment;
import com.example.loanapplication.service.closure.ClosureService;

@RestController
@RequestMapping("/api/closure")
public class ClosureController {

    private final ClosureService closureService;

    public ClosureController(ClosureService closureService) {
        this.closureService = closureService;
    }

    @PostMapping("/request")
    public ResponseEntity<ClosureResponseDto> createRequest(
            @RequestBody ForeClosureRequestDto dto) {

        return ResponseEntity.ok(
                closureService.createRequest(dto));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ForeClosureRequest>> getRequests() {

        return ResponseEntity.ok(
                closureService.getRequests());
    }

    @PostMapping("/payment")
    public ResponseEntity<LoanPayment> createPayment(
            @RequestBody LoanPaymentDto dto) {

        return ResponseEntity.ok(
                closureService.createPayment(dto));
    }

    @PostMapping("/complete")
    public ResponseEntity<LoanClosure> createClosure(
            @RequestBody LoanClosureDto dto) {

        return ResponseEntity.ok(
                closureService.createClosure(dto));
    }
}