package com.example.loanapplication.dto;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorVerifyRequest(@NotBlank String code) {}
