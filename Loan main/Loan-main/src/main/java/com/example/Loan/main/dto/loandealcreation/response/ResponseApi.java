package com.example.Loan.main.dto.loandealcreation.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseApi<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ResponseApi<T> success(
            String message,
            T data
    ) {
        return new ResponseApi<>(
                true,
                message,
                data
        );
    }

    public static <T> ResponseApi<T> failure(
            String message,
            T data
    ) {
        return new ResponseApi<>(
                false,
                message,
                data
        );
    }
}