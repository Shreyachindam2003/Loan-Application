package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Loan.main.dto.EmiPaymentDto.request.PaymentRequest;
import com.example.Loan.main.dto.EmiPaymentDto.response.CurrentEmiResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PaymentBreakupResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PaymentResponse;
import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.entity.EmiPaymentEntity.LoanPayment;
import com.example.Loan.main.entity.EmiPaymentEntity.PenaltyCharge;
import com.example.Loan.main.enums.EmiPaymentEnums.ChargeType;
import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
import com.example.Loan.main.enums.EmiPaymentEnums.PaymentStatus;
import com.example.Loan.main.enums.EmiPaymentEnums.PaymentType;
import com.example.Loan.main.exception.EmiNotFoundException;
import com.example.Loan.main.exception.InvalidPaymentException;
import com.example.Loan.main.exception.PaymentNotFoundException;
import com.example.Loan.main.exception.PaymentProcessingException;
import com.example.Loan.main.respository.EmiPaymentRepo.EmiScheduleRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.LoanPaymentRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.PenaltyChargeRepository;
import com.example.Loan.main.service.EmiPaymentService.PaymentService;
import com.example.Loan.main.util.EmiPaymentUtil.RazorpayUtil;
import com.razorpay.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final EmiScheduleRepository emiRepo;
    private final LoanPaymentRepository paymentRepo;
    private final PenaltyChargeRepository penaltyRepo;
    private final RazorpayUtil razorpayUtil;
    private final ModelMapper mapper;

    // =========================================================
    // CURRENT EMI
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public CurrentEmiResponse getCurrentEmi(Integer loanAccountId) {

        if (loanAccountId == null || loanAccountId <= 0) {
            throw new InvalidPaymentException(
                    "Valid loan account ID is required");
        }

        EmiSchedule emi = getPendingEmi(loanAccountId);
        int daysLate = getDaysLate(emi);

        BigDecimal penalty = BigDecimal.valueOf(daysLate);
        BigDecimal bonus = daysLate > 0
                ? BigDecimal.valueOf(500)
                : BigDecimal.ZERO;

        CurrentEmiResponse response =
                mapper.map(emi, CurrentEmiResponse.class);

        response.setDaysLate(daysLate);
        response.setPenaltyAmount(penalty);
        response.setBonusAmount(bonus);
        response.setTotalPayable(
                emi.getEmiAmount().add(penalty).add(bonus));

        log.info(
                "Current EMI calculated. loanAccountId={}, emiScheduleId={}, daysLate={}, totalPayable={}",
                loanAccountId, emi.getEmiScheduleId(),
                daysLate, response.getTotalPayable());

        return response;
    }

    // =========================================================
    // CREATE RAZORPAY ORDER
    // =========================================================

    @Override
    @Transactional
    public PaymentResponse createOrder(PaymentRequest req) {

        if (req == null
                || req.getLoanAccountId() == null
                || req.getLoanAccountId() <= 0
                || req.getEmiScheduleId() == null
                || req.getEmiScheduleId() <= 0) {
            throw new InvalidPaymentException(
                    "Valid loan account ID and EMI schedule ID are required");
        }

        String idempotencyKey = req.getIdempotencyKey();

        // Check idempotency before creating a new Razorpay order.
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            LoanPayment existingPayment = paymentRepo
                    .findByIdempotencyKey(idempotencyKey)
                    .orElse(null);

            if (existingPayment != null) {

                if (!req.getLoanAccountId().equals(
                        existingPayment.getLoanAccountId())
                        || !req.getEmiScheduleId().equals(
                        existingPayment.getEmiScheduleId())) {
                    throw new InvalidPaymentException(
                            "Idempotency key was already used for another payment");
                }

                log.info(
                        "Returning existing payment. paymentId={}",
                        existingPayment.getPaymentId());

                return mapper.map(
                        existingPayment, PaymentResponse.class);
            }
        }

        EmiSchedule emi = getEmi(req.getEmiScheduleId());

        if (!req.getLoanAccountId().equals(emi.getLoanAccountId())) {
            throw new InvalidPaymentException(
                    "EMI does not belong to this loan account");
        }

        if (emi.getStatus() != EmiStatus.PENDING) {
            throw new InvalidPaymentException(
                    "Only a pending EMI can be paid");
        }

        EmiSchedule currentEmi = getPendingEmi(req.getLoanAccountId());

        if (!currentEmi.getEmiScheduleId().equals(
                emi.getEmiScheduleId())) {
            throw new InvalidPaymentException(
                    "Only current EMI can be paid");
        }

        BigDecimal amount = getCurrentEmi(
                req.getLoanAccountId()).getTotalPayable();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException(
                    "Payment amount must be greater than zero");
        }

        try {
            Order order = razorpayUtil.createOrder(
                    amount,
                    "LOAN_" + req.getLoanAccountId()
                            + "_EMI_" + req.getEmiScheduleId());

            LoanPayment payment = new LoanPayment();
            payment.setLoanAccountId(req.getLoanAccountId());
            payment.setEmiScheduleId(req.getEmiScheduleId());
            payment.setAmount(amount);
            payment.setPaymentStatus(PaymentStatus.CREATED);
            payment.setPaymentType(PaymentType.EMI);
            payment.setIdempotencyKey(idempotencyKey);
            payment.setRazorpayOrderId(order.get("id"));
            payment.setPaymentDate(LocalDateTime.now());

            LoanPayment savedPayment = paymentRepo.save(payment);

            log.info(
                    "Razorpay order created. paymentId={}, loanAccountId={}, emiScheduleId={}, amount={}",
                    savedPayment.getPaymentId(),
                    req.getLoanAccountId(),
                    req.getEmiScheduleId(),
                    amount);

            return mapper.map(savedPayment, PaymentResponse.class);

        } catch (InvalidPaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "Unable to create Razorpay order. loanAccountId={}, emiScheduleId={}",
                    req.getLoanAccountId(),
                    req.getEmiScheduleId(), e);

            throw new PaymentProcessingException(
                    "Unable to create payment order", e);
        }
    }

    // =========================================================
    // VERIFY RAZORPAY PAYMENT
    // =========================================================

    @Override
    @Transactional
    public PaymentResponse verifyPayment(PaymentRequest req) {

        if (req == null
                || req.getRazorpayOrderId() == null
                || req.getRazorpayOrderId().isBlank()
                || req.getRazorpayPaymentId() == null
                || req.getRazorpayPaymentId().isBlank()
                || req.getRazorpaySignature() == null
                || req.getRazorpaySignature().isBlank()) {
            throw new InvalidPaymentException(
                    "Razorpay order ID, payment ID and signature are required");
        }

        LoanPayment payment = paymentRepo
                .findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for the supplied order ID"));

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {

            // A successful payment cannot be reused with different IDs.
            if (!req.getRazorpayPaymentId().equals(
                    payment.getRazorpayPaymentId())) {
                throw new InvalidPaymentException(
                        "Payment is already verified with another payment ID");
            }

            log.info(
                    "Payment already verified. paymentId={}",
                    payment.getPaymentId());

            return mapper.map(payment, PaymentResponse.class);
        }

        if (payment.getPaymentStatus() != PaymentStatus.CREATED) {
            throw new InvalidPaymentException(
                    "Payment is not in a verifiable state");
        }

        boolean verified;

        try {
            verified = razorpayUtil.verifyPayment(
                    req.getRazorpayOrderId(),
                    req.getRazorpayPaymentId(),
                    req.getRazorpaySignature());

        } catch (Exception e) {
            log.error(
                    "Razorpay signature verification error. paymentId={}",
                    payment.getPaymentId(), e);

            throw new PaymentProcessingException(
                    "Unable to verify Razorpay payment", e);
        }

        if (!verified) {
            log.warn(
                    "Razorpay signature verification failed. paymentId={}",
                    payment.getPaymentId());

            throw new InvalidPaymentException(
                    "Payment verification failed");
        }

        EmiSchedule emi = getEmi(payment.getEmiScheduleId());

        if (emi.getStatus() != EmiStatus.PENDING) {
            throw new InvalidPaymentException(
                    "EMI is no longer pending");
        }

        EmiSchedule currentEmi = getPendingEmi(
                payment.getLoanAccountId());

        if (!currentEmi.getEmiScheduleId().equals(
                emi.getEmiScheduleId())) {
            throw new InvalidPaymentException(
                    "This is no longer the current EMI");
        }

        int daysLate = getDaysLate(emi);
        LocalDate today = LocalDate.now();

        /*
         * Record charges only after signature verification.
         * The repository must provide the exists... method shown below.
         */
        if (daysLate > 0) {

            boolean penaltyExists = penaltyRepo
                    .existsByEmiScheduleIdAndChargeType(
                            emi.getEmiScheduleId(), ChargeType.PENALTY);

            if (!penaltyExists) {
                PenaltyCharge penalty = new PenaltyCharge();
                penalty.setLoanAccountId(payment.getLoanAccountId());
                penalty.setEmiScheduleId(emi.getEmiScheduleId());
                penalty.setChargeType(ChargeType.PENALTY);
                penalty.setAmount(BigDecimal.valueOf(daysLate));
                penalty.setChargeDate(today);
                penalty.setDaysLate(daysLate);
                penalty.setPaymentId(payment.getPaymentId());

                penaltyRepo.save(penalty);
            }

            boolean bonusExists = penaltyRepo
                    .existsByEmiScheduleIdAndChargeType(
                            emi.getEmiScheduleId(), ChargeType.BONUS);

            if (!bonusExists) {
                PenaltyCharge bonus = new PenaltyCharge();
                bonus.setLoanAccountId(payment.getLoanAccountId());
                bonus.setEmiScheduleId(emi.getEmiScheduleId());
                bonus.setChargeType(ChargeType.BONUS);
                bonus.setAmount(BigDecimal.valueOf(500));
                bonus.setChargeDate(today);
                bonus.setDaysLate(daysLate);
                bonus.setPaymentId(payment.getPaymentId());

                penaltyRepo.save(bonus);
            }
        }

        payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
        payment.setRazorpaySignature(req.getRazorpaySignature());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());

        emi.setStatus(EmiStatus.PAID);

        LoanPayment savedPayment = paymentRepo.save(payment);
        emiRepo.save(emi);

        log.info(
                "Payment verified successfully. paymentId={}, loanAccountId={}, emiScheduleId={}, daysLate={}",
                savedPayment.getPaymentId(),
                savedPayment.getLoanAccountId(),
                emi.getEmiScheduleId(),
                daysLate);

        return mapper.map(savedPayment, PaymentResponse.class);
    }

    // =========================================================
    // GET PAYMENT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Integer paymentId) {

        if (paymentId == null || paymentId <= 0) {
            throw new InvalidPaymentException(
                    "Valid payment ID is required");
        }

        LoanPayment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found: " + paymentId));

        return mapper.map(payment, PaymentResponse.class);
    }

    // =========================================================
    // PAYMENT HISTORY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(
            Integer loanAccountId) {

        if (loanAccountId == null || loanAccountId <= 0) {
            throw new InvalidPaymentException(
                    "Valid loan account ID is required");
        }

        List<PaymentResponse> history = paymentRepo
                .findByLoanAccountIdOrderByPaymentDateDesc(loanAccountId)
                .stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .map(p -> mapper.map(p, PaymentResponse.class))
                .toList();

        log.info(
                "Payment history fetched. loanAccountId={}, successfulPayments={}",
                loanAccountId, history.size());

        return history;
    }

    // =========================================================
    // PAYMENT BREAKUP
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PaymentBreakupResponse getPaymentBreakup(
            Integer emiScheduleId) {

        EmiSchedule emi = getEmi(emiScheduleId);

        List<PenaltyCharge> charges =
                penaltyRepo.findByEmiScheduleId(emiScheduleId);

        BigDecimal penalty = charges.stream()
                .filter(c -> c.getChargeType() == ChargeType.PENALTY)
                .map(PenaltyCharge::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal bonus = charges.stream()
                .filter(c -> c.getChargeType() == ChargeType.BONUS)
                .map(PenaltyCharge::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PaymentBreakupResponse response =
                mapper.map(emi, PaymentBreakupResponse.class);

        response.setPenaltyAmount(penalty);
        response.setBonusAmount(bonus);
        response.setDaysLate(getDaysLate(emi));
        response.setTotalAmount(
                emi.getEmiAmount().add(penalty).add(bonus));

        return response;
    }

    // =========================================================
    // GET EMI
    // =========================================================

    private EmiSchedule getEmi(Integer id) {

        if (id == null || id <= 0) {
            throw new InvalidPaymentException(
                    "Valid EMI schedule ID is required");
        }

        return emiRepo.findById(id)
                .orElseThrow(() -> new EmiNotFoundException(
                        "EMI schedule not found: " + id));
    }

    // =========================================================
    // GET CURRENT / PENDING EMI
    // =========================================================

    private EmiSchedule getPendingEmi(Integer loanAccountId) {

        if (loanAccountId == null || loanAccountId <= 0) {
            throw new InvalidPaymentException(
                    "Valid loan account ID is required");
        }

        return emiRepo
                .findFirstByLoanAccountIdAndStatusOrderByInstallmentNoAsc(
                        loanAccountId, EmiStatus.PENDING)
                .orElseThrow(() -> new EmiNotFoundException(
                        "No pending EMI found for loan account: "
                                + loanAccountId));
    }

    // =========================================================
    // CALCULATE LATE DAYS
    // =========================================================

    private int getDaysLate(EmiSchedule emi) {

        LocalDate dueDate = emi.getDueDate();

        if (dueDate == null) {
            throw new InvalidPaymentException(
                    "EMI due date is missing");
        }

        LocalDate today = LocalDate.now();

        if (today.isAfter(dueDate)) {
            return Math.toIntExact(
                    ChronoUnit.DAYS.between(dueDate, today));
        }

        return 0;
    }

    // =========================================================
    // INVOICE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PaymentBreakupResponse getInvoice(Integer paymentId) {

        if (paymentId == null || paymentId <= 0) {
            throw new InvalidPaymentException(
                    "Valid payment ID is required");
        }

        LoanPayment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found: " + paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentException(
                    "Invoice is available only for successful payments");
        }

        return getPaymentBreakup(payment.getEmiScheduleId());
    }
}
