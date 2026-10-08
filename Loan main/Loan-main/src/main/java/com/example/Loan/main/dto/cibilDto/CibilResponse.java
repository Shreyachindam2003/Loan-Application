package com.example.Loan.main.dto.cibilDto;


import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class CibilResponse {

    private Integer CibilScore;
    private LocalDateTime CheckDate;
    private String Status;



}
