package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.Loan.main.dto.EmiPaymentDto.request.CreatePaymentRequest;
import com.example.Loan.main.dto.EmiPaymentDto.request.VerifyPaymentRequest;
import com.example.Loan.main.dto.EmiPaymentDto.response.*;
import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.entity.EmiPaymentEntity.LoanAccount;
import com.example.Loan.main.entity.EmiPaymentEntity.LoanPayment;
import com.example.Loan.main.entity.EmiPaymentEntity.PenaltyCharge;
import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
import com.example.Loan.main.enums.EmiPaymentEnums.PaymentStatus;
import com.example.Loan.main.enums.EmiPaymentEnums.PaymentType;
import com.example.Loan.main.exception.EmiPaymentException.PaymentException;
import com.example.Loan.main.exception.EmiPaymentException.ResourceNotFoundException;
import com.example.Loan.main.respository.EmiPaymentRepo.EmiScheduleRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.LoanAccountRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.LoanPaymentRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.PenaltyChargeRepository;
import com.example.Loan.main.service.EmiPaymentService.PaymentService;
import com.example.Loan.main.util.EmiPaymentUtil.RazorpayUtil;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final LoanAccountRepository loanAccountRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final PenaltyChargeRepository penaltyChargeRepository;
    private final RazorpayUtil razorpayUtil;
    private final ModelMapper modelMapper;

    @Override
    public CurrentEmiResponse getCurrentEmi(Integer loanAccountId) {

        LoanAccount loanAccount=getActiveLoan(loanAccountId);

        EmiSchedule currentEmi=emiScheduleRepository
                .findFirstByLoanAccountIdAndPaymentStatusOrderByDueDateAsc(
                        loanAccountId,
                        EmiStatus.PENDING.name())
                .orElseThrow(() -> new ResourceNotFoundException("No pending EMI found"));

        BigDecimal penaltyAmount=getPendingCharge(
                currentEmi.getEmiScheduleId(),"PENALTY");

        BigDecimal bonusAmount=getPendingCharge(
                currentEmi.getEmiScheduleId(),"BONUS");

        BigDecimal totalAmount=currentEmi.getEmi()
                .add(penaltyAmount)
                .add(bonusAmount);

        boolean overdue=currentEmi.getDueDate().isBefore(LocalDate.now());

//      CurrentEmiResponse currentEmiResponse=modelMapper.map(currentEmi,CurrentEmiResponse.class);

        return CurrentEmiResponse.builder()
                .emiScheduleId(currentEmi.getEmiScheduleId())
                .loanAccountNo(loanAccount.getLoanAccountNo())
                .installmentNo(currentEmi.getInstallmentNo())
                .emiAmount(currentEmi.getEmi())
                .penaltyAmount(penaltyAmount)
                .bonusAmount(bonusAmount)
                .totalAmount(totalAmount)
                .dueDate(currentEmi.getDueDate())
                .status(overdue ? "OVERDUE" : "PENDING")
                .canPay(!currentEmi.getDueDate().isAfter(LocalDate.now()))
                .build();
    }

    @Override
    public PaymentResponse createPaymentOrder(CreatePaymentRequest request) {
        LoanAccount loanAccount=getActiveLoan(request.getLoanAccountId());

        LoanPayment existingPayment=loanPaymentRepository
                .findByIdempotencyKey(request.getIdempotencyKey())
                .orElse(null);

        if(existingPayment!=null){
            return mapPaymentResponse(existingPayment);
        }

        List<EmiSchedule> payableEmis=getPayableEmis(loanAccount.getLoanAccountId());

        if(payableEmis.isEmpty()){
            throw new ResourceNotFoundException("No pending EMI found");
        }

        BigDecimal totalEmiAmount=BigDecimal.ZERO;
        BigDecimal totalPenaltyAmount=BigDecimal.ZERO;
        BigDecimal totalBonusAmount=BigDecimal.ZERO;

        for(EmiSchedule emi:payableEmis){
            totalEmiAmount=totalEmiAmount.add(emi.getEmi());
            totalPenaltyAmount=totalPenaltyAmount.add(getPendingCharge(emi.getEmiScheduleId(),"PENALTY"));
            totalBonusAmount=totalBonusAmount.add(getPendingCharge(emi.getEmiScheduleId(),"BONUS"));
        }

        BigDecimal totalAmount=totalEmiAmount.add(totalPenaltyAmount).add(totalBonusAmount);

        String paymentType=totalPenaltyAmount.signum()>0||totalBonusAmount.signum()>0
                ?PaymentType.EMI_WITH_CHARGES.name()
                :PaymentType.EMI.name();

        long amountInPaise=totalAmount.multiply(BigDecimal.valueOf(100)).longValueExact();

        String receipt="LOAN-"+loanAccount.getLoanAccountId()+"-EMI-"+payableEmis.get(0).getInstallmentNo();

        String razorpayOrderId=razorpayUtil.createOrder(amountInPaise,receipt);

        LoanPayment payment=new LoanPayment();

        payment.setLoanAccountId(loanAccount.getLoanAccountId());
        payment.setPaymentAmount(totalAmount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.PENDING.name());
        payment.setPaymentName("EMI Payment - "+payableEmis.size()+" Installment(s)");
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setPaymentType(paymentType);

        LoanPayment savedPayment=loanPaymentRepository.save(payment);

        return mapPaymentResponse(savedPayment);
    }

    @Override
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        LoanPayment payment=loanPaymentRepository.findById(request.getPaymentId())
                .orElseThrow(()->new ResourceNotFoundException("Payment not found"));

        if(PaymentStatus.SUCCESS.name().equals(payment.getPaymentStatus())){
            return mapPaymentResponse(payment);
        }

        if(!payment.getRazorpayOrderId().equals(request.getRazorpayOrderId())){
            throw new PaymentException("Razorpay order ID does not match");
        }

        boolean verified=razorpayUtil.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());

        if(!verified){
            payment.setPaymentStatus(PaymentStatus.FAILED.name());
            loanPaymentRepository.save(payment);
            throw new PaymentException("Razorpay payment verification failed");
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaymentStatus(PaymentStatus.SUCCESS.name());
        payment.setPaymentDate(LocalDateTime.now());

        loanPaymentRepository.save(payment);

        List<EmiSchedule> payableEmis=getPayableEmis(payment.getLoanAccountId());

        if(payableEmis.isEmpty()){
            throw new ResourceNotFoundException("Pending EMI not found");
        }

        BigDecimal totalPrincipalPaid=BigDecimal.ZERO;

        for(EmiSchedule emi:payableEmis){
            emi.setPaymentStatus(EmiStatus.PAID.name());
            emi.setPaidDate(LocalDateTime.now());
            emi.setPaymentId(payment.getPaymentId());

            emiScheduleRepository.save(emi);

            totalPrincipalPaid=totalPrincipalPaid.add(emi.getPrincipalAmount());

            markChargesPaid(emi.getEmiScheduleId(),payment.getPaymentId());
        }

        updateLoanAccount(
                payment.getLoanAccountId(),
                totalPrincipalPaid,
                payment.getPaymentAmount());

        return mapPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly=true)
    public PaymentResponse getPaymentById(Integer paymentId) {
        LoanPayment payment=loanPaymentRepository.findById(paymentId)
                .orElseThrow(()->new ResourceNotFoundException("Payment not found"));

        return mapPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly=true)
    public Page<PaymentHistoryResponse> getPaymentHistory(Integer loanAccountId,Pageable pageable) {
        getActiveLoan(loanAccountId);

        Page<LoanPayment> payments=loanPaymentRepository
                .findByLoanAccountIdAndStatus(
                        loanAccountId,
                        PaymentStatus.SUCCESS.name(),
                        pageable);

        List<EmiSchedule> schedules=emiScheduleRepository
                .findByLoanAccountIdOrderByInstallmentNoAsc(loanAccountId);

        Map<Integer,List<EmiSchedule>> paymentEmiMap=schedules.stream()
                .filter(e->e.getPaymentId()!=null)
                .collect(Collectors.groupingBy(EmiSchedule::getPaymentId));

        return payments.map(payment->{
            List<EmiSchedule> paidEmis=paymentEmiMap
                    .getOrDefault(payment.getPaymentId(),List.of());

            List<Integer> installmentNos=paidEmis.stream()
                    .map(EmiSchedule::getInstallmentNo)
                    .toList();

            BigDecimal emiAmount=paidEmis.stream()
                    .map(EmiSchedule::getEmi)
                    .reduce(BigDecimal.ZERO,BigDecimal::add);

            BigDecimal penaltyAmount=BigDecimal.ZERO;
            BigDecimal bonusAmount=BigDecimal.ZERO;

            for(EmiSchedule emi:paidEmis){
                penaltyAmount=penaltyAmount.add(
                        getPaidCharge(emi.getEmiScheduleId(),"PENALTY"));

                bonusAmount=bonusAmount.add(
                        getPaidCharge(emi.getEmiScheduleId(),"BONUS"));
            }

            return PaymentHistoryResponse.builder()
                    .paymentId(payment.getPaymentId())
                    .installmentNos(installmentNos)
                    .emiAmount(emiAmount)
                    .penaltyAmount(penaltyAmount)
                    .bonusAmount(bonusAmount)
                    .totalAmount(payment.getPaymentAmount())
                    .paymentDate(payment.getPaymentDate())
                    .paymentStatus(payment.getPaymentStatus())
                    .build();
        });
    }

    @Override
    @Transactional(readOnly=true)
    public PenaltyBreakupResponse getPaymentBreakup(Integer emiScheduleId) {
        EmiSchedule emi=emiScheduleRepository.findById(emiScheduleId)
                .orElseThrow(()->new ResourceNotFoundException("EMI schedule not found"));

        List<PenaltyCharge> charges=penaltyChargeRepository
                .findByEmiScheduleIdOrderByChargeDateAsc(emiScheduleId);

        BigDecimal penaltyAmount=charges.stream()
                .filter(c->"PENALTY".equals(c.getChargeType())&&"PENDING".equals(c.getStatus()))
                .map(PenaltyCharge::getPenaltyAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal bonusAmount=charges.stream()
                .filter(c->"BONUS".equals(c.getChargeType())&&"PENDING".equals(c.getStatus()))
                .map(PenaltyCharge::getPenaltyAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        List<PenaltyDetailResponse> penaltyDetails=charges.stream()
                .filter(c->"PENALTY".equals(c.getChargeType())&&"PENDING".equals(c.getStatus()))
                .map(this::mapPenaltyDetail)
                .toList();

        List<PenaltyDetailResponse> bonusDetails=charges.stream()
                .filter(c->"BONUS".equals(c.getChargeType())&&"PENDING".equals(c.getStatus()))
                .map(this::mapPenaltyDetail)
                .toList();

        BigDecimal totalAmount=emi.getEmi().add(penaltyAmount).add(bonusAmount);

        return PenaltyBreakupResponse.builder()
                .emiAmount(emi.getEmi())
                .penaltyAmount(penaltyAmount)
                .bonusAmount(bonusAmount)
                .totalAmount(totalAmount)
                .penaltyDetails(penaltyDetails)
                .bonusDetails(bonusDetails)
                .build();
    }

    @Override
    @Transactional(readOnly=true)
    public InvoiceResponse generatePaymentInvoice(Integer paymentId) {
        LoanPayment payment=loanPaymentRepository.findById(paymentId)
                .orElseThrow(()->new ResourceNotFoundException("Payment not found"));

        LoanAccount loanAccount=loanAccountRepository.findById(payment.getLoanAccountId())
                .orElseThrow(()->new ResourceNotFoundException("Loan account not found"));

        List<EmiSchedule> schedules=emiScheduleRepository
                .findByLoanAccountIdOrderByInstallmentNoAsc(payment.getLoanAccountId());

        List<EmiSchedule> paidEmis=schedules.stream()
                .filter(e->paymentId.equals(e.getPaymentId()))
                .toList();

        BigDecimal emiAmount=paidEmis.stream()
                .map(EmiSchedule::getEmi)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal penaltyAmount=BigDecimal.ZERO;
        BigDecimal bonusAmount=BigDecimal.ZERO;

        for(EmiSchedule emi:paidEmis){
            penaltyAmount=penaltyAmount.add(getPaidCharge(emi.getEmiScheduleId(),"PENALTY"));
            bonusAmount=bonusAmount.add(getPaidCharge(emi.getEmiScheduleId(),"BONUS"));
        }

        if(paidEmis.isEmpty()){
            emiAmount=payment.getPaymentAmount();
        }

        return InvoiceResponse.builder()
                .paymentId(payment.getPaymentId())
                .loanAccountNo(loanAccount.getLoanAccountNo())
                .customerName("Customer "+loanAccount.getCustomerId())
                .emiAmount(emiAmount)
                .penaltyAmount(penaltyAmount)
                .bonusAmount(bonusAmount)
                .totalAmount(payment.getPaymentAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentStatus(payment.getPaymentStatus())
                .build();
    }

    private List<EmiSchedule> getPayableEmis(Integer loanAccountId) {
        return emiScheduleRepository
                .findByLoanAccountIdOrderByInstallmentNoAsc(loanAccountId)
                .stream()
                .filter(e->EmiStatus.PENDING.name().equals(e.getPaymentStatus()))
                .filter(e->!e.getDueDate().isAfter(LocalDate.now()))
                .toList();
    }

    private LoanAccount getActiveLoan(Integer loanAccountId) {
        return loanAccountRepository
                .findByLoanAccountIdAndLoanStatus(loanAccountId,"ACTIVE")
                .orElseThrow(()->new ResourceNotFoundException("Active loan account not found"));
    }

    private BigDecimal getPendingCharge(Integer emiScheduleId,String chargeType) {
        BigDecimal amount=penaltyChargeRepository.getPendingChargeAmount(emiScheduleId,chargeType);
        return amount!=null?amount:BigDecimal.ZERO;
    }

    private BigDecimal getPaidCharge(Integer emiScheduleId,String chargeType) {
        List<PenaltyCharge> charges=penaltyChargeRepository
                .findByEmiScheduleIdAndChargeType(emiScheduleId,chargeType);

        return charges.stream()
                .filter(c->"PAID".equals(c.getStatus()))
                .map(PenaltyCharge::getPenaltyAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    private void markChargesPaid(Integer emiScheduleId,Integer paymentId) {
        List<PenaltyCharge> charges=penaltyChargeRepository.findPendingCharges(emiScheduleId);

        for(PenaltyCharge charge:charges){
            charge.setStatus("PAID");
            charge.setPaymentId(paymentId);
            penaltyChargeRepository.save(charge);
        }
    }

    private void updateLoanAccount(Integer loanAccountId,BigDecimal principalPaid,BigDecimal totalPayment) {
        LoanAccount loanAccount=loanAccountRepository.findById(loanAccountId)
                .orElseThrow(()->new ResourceNotFoundException("Loan account not found"));

        BigDecimal outstanding=loanAccount.getOutstandingPrincipal().subtract(principalPaid);

        if(outstanding.signum()<0){
            outstanding=BigDecimal.ZERO;
        }

        loanAccount.setOutstandingPrincipal(outstanding);

        BigDecimal totalPaid=loanAccount.getTotalPaidAmount().add(totalPayment);
        loanAccount.setTotalPaidAmount(totalPaid);

        loanAccountRepository.save(loanAccount);
    }

    private PenaltyDetailResponse mapPenaltyDetail(PenaltyCharge charge) {
        return modelMapper.map(charge,PenaltyDetailResponse.class);
    }

    private PaymentResponse mapPaymentResponse(LoanPayment payment) {
        return modelMapper.map(payment,PaymentResponse.class);
    }
}