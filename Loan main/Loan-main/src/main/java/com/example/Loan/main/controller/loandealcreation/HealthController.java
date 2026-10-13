package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseApi<Map<String, String>> health() {
        return ResponseApi.success(
                "Loan module is running",
                Map.of(
                        "application", "Loan-main",
                        "status", "UP"
                )
        );
    }
}