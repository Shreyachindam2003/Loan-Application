
package com.example.Loan.main.controller;

import java.util.List;

import com.example.Loan.main.dto.ClosureResponseDto;
import com.example.Loan.main.dto.ForeClosureRequestDto;
import com.example.Loan.main.dto.LoanClosureDto;
import com.example.Loan.main.entity.ForeClosureRequest;
import com.example.Loan.main.entity.LoanClosure;
import com.example.Loan.main.service.ClosureService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/closure")
@RequiredArgsConstructor
public class ClosureController {

    private final ClosureService closureService;

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
        return closureService.approveRequest(requestId);
    }

    @PatchMapping("/requests/{requestId}/reject")
    public ClosureResponseDto rejectRequest(
            @PathVariable Integer requestId) {
        return closureService.rejectRequest(requestId);
    }

}
