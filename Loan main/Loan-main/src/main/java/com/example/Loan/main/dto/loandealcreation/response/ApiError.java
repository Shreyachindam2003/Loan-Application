package com.example.Loan.main.dto.loandealcreation.response;

import java.util.List;

public record ApiError(
        String code,
        List<String> details
) {
    //dada
    public ApiError {
        details = details == null
                ? List.of()
                : List.copyOf(details);
    }
}