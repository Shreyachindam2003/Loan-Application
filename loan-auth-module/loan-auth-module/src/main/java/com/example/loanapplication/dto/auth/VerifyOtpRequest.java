package com.example.loanapplication.dto.auth;

import jakarta.validation.constraints.*;

public record VerifyOtpRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 6) String otp
) {}
