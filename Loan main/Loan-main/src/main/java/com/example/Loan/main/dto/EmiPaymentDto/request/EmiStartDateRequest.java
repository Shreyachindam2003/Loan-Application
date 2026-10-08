package com.example.Loan.main.dto.EmiPaymentDto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiStartDateRequest {

    @NotNull(message = "EMI start date is required")
    private LocalDate emiStartDate;
}
