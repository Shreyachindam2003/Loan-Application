package com.example.Loan.main.dto.response;


import java.math.BigDecimal;
import java.time.LocalDateTime;


import com.example.Loan.main.enums.PaymentStatus;
import com.example.Loan.main.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponse {

    private Integer paymentId;
    private Integer loanAccountId;
    private Integer emiScheduleId;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private PaymentType paymentType;

    private String razorpayOrderId;
    private String razorpayPaymentId;

    private LocalDateTime paymentDate;
}
