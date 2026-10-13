package com.example.Loan.main.dto.loandealcreation.response;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        ApiError error,
        Instant timestamp,
        String requestId
) {

    public static <T> ApiResponse<T> success(
            String message,
            T data,
            String requestId
    ) {
        return new ApiResponse<>(
                true,
                message,
                data,
                null,
                Instant.now(),
                requestId
        );
    }
    //dada
    public static <T> ApiResponse<T> failure(
            String message,
            String errorCode,
            List<String> details,
            String requestId
    ) {
        return new ApiResponse<>(
                false,
                message,
                null,
                new ApiError(errorCode, details),
                Instant.now(),
                requestId
        );
    }
}