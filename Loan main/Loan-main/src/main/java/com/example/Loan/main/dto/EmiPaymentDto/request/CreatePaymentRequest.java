package com.example.Loan.main.dto.EmiPaymentDto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {

    @NotNull(message = "Loan account id is required")
    private Integer loanAccountId;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}
