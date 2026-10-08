package com.example.loanapplication.dto.auth;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RegisterRequest(
        @NotBlank String firstName,
        String lastName,
        @NotNull @Min(18) Integer age,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String mobileNumber,
        @NotBlank String panNo,
        @NotBlank String aadhaarNo,
        @NotBlank String employmentType,
        @NotNull @DecimalMin("0.0") BigDecimal salary,
        @NotNull @DecimalMin("0.0") BigDecimal monthlyInvestment
) {}
