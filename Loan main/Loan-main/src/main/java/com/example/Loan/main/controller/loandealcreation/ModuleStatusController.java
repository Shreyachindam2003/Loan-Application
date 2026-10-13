package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.ApiResponse;
import com.example.Loan.main.dto.loandealcreation.response.ModuleStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/module-status")
public class ModuleStatusController {

    @GetMapping
    public ResponseEntity<ApiResponse<ModuleStatusResponse>> getStatus() {
        String requestId = UUID.randomUUID().toString();
//dada
        ModuleStatusResponse data = new ModuleStatusResponse(
                "Loan origination and funding",
                "RUNNING"
        );

        return ResponseEntity.ok()
                .header("X-Request-Id", requestId)
                .body(ApiResponse.success(
                        "Loan module API is running",
                        data,
                        requestId
                ));
    }
}