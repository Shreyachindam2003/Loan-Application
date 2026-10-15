package com.example.Loan.main.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OAuth2TwoFactorRequest(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "[0-9]{6}", message = "Code must contain 6 digits") String code
) {}
