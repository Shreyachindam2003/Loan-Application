package com.example.Loan.main.controller.closure;

import com.example.Loan.main.dto.closure.ClosureResponseDto;
import com.example.Loan.main.dto.closure.ForeClosureRequestDto;
import com.example.Loan.main.dto.closure.LoanClosureDto;
import com.example.Loan.main.dto.closure.LoanPaymentDto;
import com.example.Loan.main.entity.closure.ForeClosureRequest;
import com.example.Loan.main.entity.closure.LoanClosure;
import com.example.Loan.main.entity.closure.LoanPayment;
import com.example.Loan.main.service.closure.ClosureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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