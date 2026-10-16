package com.example.Loan.main.serviceImpl.loandealcreation;

import com.example.Loan.main.exception.loandealcreation.BusinessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

final class PaginationSupport {

    private PaginationSupport() {
    }

    static PageRequest page(
            int page,
            int size,
            String... sortFields
    ) {
        if (page < 0) {
            throw new BusinessException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new BusinessException(
                    "Page size must be between 1 and 100"
            );
        }

        if (sortFields.length == 0) {
            return PageRequest.of(page, size);
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, sortFields)
        );
    }
}