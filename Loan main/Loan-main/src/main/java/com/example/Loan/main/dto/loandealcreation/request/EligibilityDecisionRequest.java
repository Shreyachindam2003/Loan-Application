package com.example.Loan.main.dto.loandealcreation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EligibilityDecisionRequest(

        @NotBlank(message = "Decision is required")
        @Pattern(
                regexp = "APPROVED|REJECTED",
                message = "Decision must be APPROVED or REJECTED"
        )
        String decision,

        @Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
        String remarks

) {
}