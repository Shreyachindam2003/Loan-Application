
package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final EmiScheduleRepository emiRepo;
    private final LoanPaymentRepository paymentRepo;
    private final PenaltyChargeRepository penaltyRepo;
    private final RazorpayUtil razorpayUtil;
    private final ModelMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public CurrentEmiResponse getCurrentEmi(Integer loanAccountId) {

        if (loanAccountId == null || loanAccountId <= 0) {
            throw new InvalidPaymentException("Valid loan account ID is required");
        }

        EmiSchedule emi = getCurrentDueEmi(loanAccountId);
        int daysLate = getDaysLate(emi);

        CurrentEmiResponse response = mapper.map(emi, CurrentEmiResponse.class);
        response.setStatus(emi.getStatus());
        response.setDaysLate(daysLate);
        response.setPenaltyAmount(BigDecimal.valueOf(daysLate));
        response.setBonusAmount(calculateBonus(emi, daysLate));
        response.setTotalPayable(
                emi.getEmiAmount()
                        .add(response.getPenaltyAmount())
                        .add(response.getBonusAmount()));

        return response;
    }

    @Override
    @Transactional
    public PaymentResponse createOrder(PaymentRequest req) {

        if (req == null || req.getLoanAccountId() == null
                || req.getLoanAccountId() <= 0
                || req.getEmiScheduleId() == null
                || req.getEmiScheduleId() <= 0) {
            throw new InvalidPaymentException(
                    "Valid loan account ID and EMI schedule ID are required");
        }

        String key = req.getIdempotencyKey();

        if (key != null && !key.isBlank()) {
            LoanPayment existing = paymentRepo.findByIdempotencyKey(key).orElse(null);

            if (existing != null) {
                if (!req.getLoanAccountId().equals(existing.getLoanAccountId())
                        || !req.getEmiScheduleId().equals(existing.getEmiScheduleId())) {
                    throw new InvalidPaymentException(
                            "Idempotency key was already used for another payment");
                }

                log.info("Returning existing payment. paymentId={}",
                        existing.getPaymentId());
                return mapper.map(existing, PaymentResponse.class);
            }
        }

        EmiSchedule emi = getEmi(req.getEmiScheduleId());

        if (!req.getLoanAccountId().equals(emi.getLoanAccountId())) {
            throw new InvalidPaymentException("EMI does not belong to this loan account");
        }
        if (emi.getStatus() != EmiStatus.PENDING) {
            throw new InvalidPaymentException("Only a pending EMI can be paid");
        }
        if (!getCurrentDueEmi(req.getLoanAccountId())
                .getEmiScheduleId().equals(emi.getEmiScheduleId())) {
            throw new InvalidPaymentException("Only the currently due EMI can be paid");
        }

        BigDecimal amount = getCurrentEmi(req.getLoanAccountId()).getTotalPayable();

        if (amount == null || amount.compareTo(ZERO) <= 0) {
            throw new InvalidPaymentException("Payment amount must be greater than zero");
        }

        try {
            Order order = razorpayUtil.createOrder(amount,
                    "LOAN_" + req.getLoanAccountId() + "_EMI_" + req.getEmiScheduleId());

            LoanPayment payment = new LoanPayment();
            payment.setLoanAccountId(req.getLoanAccountId());
            payment.setEmiScheduleId(req.getEmiScheduleId());
            payment.setAmount(amount);
            payment.setPaymentStatus(PaymentStatus.CREATED);
            payment.setPaymentType(PaymentType.EMI);
            payment.setIdempotencyKey(key);
            payment.setRazorpayOrderId(order.get("id"));
            payment.setPaymentDate(LocalDateTime.now());

            LoanPayment saved = paymentRepo.save(payment);

            log.info("Razorpay order created. paymentId={}, loanAccountId={}, amount={}",
                    saved.getPaymentId(), req.getLoanAccountId(), amount);

            return mapper.map(saved, PaymentResponse.class);

        } catch (InvalidPaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unable to create Razorpay order for loan {}",
                    req.getLoanAccountId(), e);
            throw new PaymentProcessingException("Unable to create payment order", e);
        }
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(PaymentRequest req) {

        if (req == null || req.getRazorpayOrderId() == null
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
            if (!req.getRazorpayPaymentId().equals(payment.getRazorpayPaymentId())) {
                throw new InvalidPaymentException(
                        "Payment is already verified with another payment ID");
            }
            return mapper.map(payment, PaymentResponse.class);
        }

        if (payment.getPaymentStatus() != PaymentStatus.CREATED) {
            throw new InvalidPaymentException("Payment is not in a verifiable state");
        }

        boolean verified;
        try {
            verified = razorpayUtil.verifyPayment(
                    req.getRazorpayOrderId(),
                    req.getRazorpayPaymentId(),
                    req.getRazorpaySignature());
        } catch (Exception e) {
            log.error("Razorpay verification error. paymentId={}",
                    payment.getPaymentId(), e);
            throw new PaymentProcessingException("Unable to verify Razorpay payment", e);
        }

        if (!verified) {
            log.warn("Payment verification failed. paymentId={}", payment.getPaymentId());
            throw new InvalidPaymentException("Payment verification failed");
        }

        EmiSchedule emi = getEmi(payment.getEmiScheduleId());

        if (emi.getStatus() != EmiStatus.PENDING) {
            throw new InvalidPaymentException("EMI is no longer pending");
        }
        if (!getCurrentDueEmi(payment.getLoanAccountId())
                .getEmiScheduleId().equals(emi.getEmiScheduleId())) {
            throw new InvalidPaymentException("This is no longer the currently due EMI");
        }

        int daysLate = getDaysLate(emi);
        LocalDate today = LocalDate.now();

        if (daysLate > 0) {
            saveCharge(payment, emi, ChargeType.PENALTY,
                    BigDecimal.valueOf(daysLate), daysLate, today);

            BigDecimal bonus = calculateBonus(emi, daysLate);
            if (bonus.compareTo(ZERO) > 0) {
                saveCharge(payment, emi, ChargeType.BONUS, bonus, daysLate, today);
            }
        }

        payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
        payment.setRazorpaySignature(req.getRazorpaySignature());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());
        emi.setStatus(EmiStatus.PAID);

        LoanPayment saved = paymentRepo.save(payment);
        emiRepo.save(emi);

        log.info("Payment successful. paymentId={}, loanAccountId={}, emiScheduleId={}",
                saved.getPaymentId(), saved.getLoanAccountId(), emi.getEmiScheduleId());

        return mapper.map(saved, PaymentResponse.class);
    }

    private void saveCharge(LoanPayment payment, EmiSchedule emi,
                            ChargeType type, BigDecimal amount, int daysLate, LocalDate today) {

        if (penaltyRepo.existsByEmiScheduleIdAndChargeType(
                emi.getEmiScheduleId(), type)) {
            return;
        }

        PenaltyCharge charge = new PenaltyCharge();
        charge.setLoanAccountId(payment.getLoanAccountId());
        charge.setEmiScheduleId(emi.getEmiScheduleId());
        charge.setChargeType(type);
        charge.setAmount(amount);
        charge.setChargeDate(today);
        charge.setDaysLate(daysLate);
        charge.setPaymentId(payment.getPaymentId());
        penaltyRepo.save(charge);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Integer paymentId) {

        if (paymentId == null || paymentId <= 0) {
            throw new InvalidPaymentException("Valid payment ID is required");
        }

        LoanPayment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found: " + paymentId));

        return mapper.map(payment, PaymentResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(Integer loanAccountId) {

        if (loanAccountId == null || loanAccountId <= 0) {
            throw new InvalidPaymentException("Valid loan account ID is required");
        }

        List<PaymentResponse> history = paymentRepo
                .findByLoanAccountIdOrderByPaymentDateDesc(loanAccountId)
                .stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .map(p -> mapper.map(p, PaymentResponse.class))
                .toList();

        log.info("Payment history fetched. loanAccountId={}, count={}",
                loanAccountId, history.size());

        return history;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentBreakupResponse getPaymentBreakup(Integer emiScheduleId) {

        EmiSchedule emi = getEmi(emiScheduleId);
        int daysLate = getDaysLate(emi);

        PaymentBreakupResponse response = mapper.map(emi, PaymentBreakupResponse.class);
        response.setPenaltyAmount(BigDecimal.valueOf(daysLate));
        response.setBonusAmount(calculateBonus(emi, daysLate));
        response.setDaysLate(daysLate);
        response.setTotalAmount(emi.getEmiAmount()
                .add(response.getPenaltyAmount())
                .add(response.getBonusAmount()));

        return response;
    }

    private EmiSchedule getEmi(Integer id) {

        if (id == null || id <= 0) {
            throw new InvalidPaymentException("Valid EMI schedule ID is required");
        }

        return emiRepo.findById(id)
                .orElseThrow(() -> new EmiNotFoundException(
                        "EMI schedule not found: " + id));
    }

    private EmiSchedule getCurrentDueEmi(Integer loanAccountId) {

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        return emiRepo
                .findByLoanAccountIdAndStatusOrderByInstallmentNoAsc(
                        loanAccountId, EmiStatus.PENDING)
                .stream()
                .filter(emi -> emi.getDueDate() != null)
                .filter(emi -> !emi.getDueDate().isAfter(today)
                        || !YearMonth.from(emi.getDueDate()).isAfter(currentMonth))
                .findFirst()
                .orElseThrow(() -> new EmiNotFoundException(
                        "No EMI is currently available for payment"));
    }

    private int getDaysLate(EmiSchedule emi) {

        if (emi.getDueDate() == null) {
            throw new InvalidPaymentException("EMI due date is missing");
        }

        LocalDate today = LocalDate.now();
        return today.isAfter(emi.getDueDate())
                ? Math.toIntExact(ChronoUnit.DAYS.between(emi.getDueDate(), today))
                : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentBreakupResponse getInvoice(Integer paymentId) {

        if (paymentId == null || paymentId <= 0) {
            throw new InvalidPaymentException("Valid payment ID is required");
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

    private BigDecimal calculateBonus(EmiSchedule emi, int daysLate) {

        if (daysLate <= 0) {
            return ZERO;
        }

        long lateMonths = ChronoUnit.MONTHS.between(
                YearMonth.from(emi.getDueDate()),
                YearMonth.from(LocalDate.now()));

        return BigDecimal.valueOf(Math.max(0, lateMonths) * 500);
    }
}
