
package com.example.Loan.main.serviceImpl.EmiPaymentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyResponse;
import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.entity.EmiPaymentEntity.PenaltyCharge;
import com.example.Loan.main.enums.EmiPaymentEnums.ChargeType;
import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
import com.example.Loan.main.exception.PenaltyNotFoundException;
import com.example.Loan.main.respository.EmiPaymentRepo.EmiScheduleRepository;
import com.example.Loan.main.respository.EmiPaymentRepo.PenaltyChargeRepository;
import com.example.Loan.main.service.EmiPaymentService.PenaltyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyServiceImpl implements PenaltyService {

    private final PenaltyChargeRepository penaltyRepo;
    private final EmiScheduleRepository emiRepo;
    private final ModelMapper mapper;

    @Override
    public List<PenaltyResponse> getPenalties(Integer loanAccountId) {

        if (loanAccountId == null || loanAccountId <= 0) {
            throw new IllegalArgumentException("Invalid loan account ID");
        }

        Map<Integer, PenaltyResponse> result = new LinkedHashMap<>();

        // Existing saved charges
        for (PenaltyCharge charge : penaltyRepo.findByLoanAccountId(loanAccountId)) {
            Integer emiId = charge.getEmiScheduleId();

            PenaltyResponse dto = result.computeIfAbsent(emiId, id -> {
                PenaltyResponse r = mapper.map(charge, PenaltyResponse.class);
                r.setPenaltyAmount(BigDecimal.ZERO);
                r.setBonusAmount(BigDecimal.ZERO);
                return r;
            });

            BigDecimal amount = charge.getAmount() == null
                    ? BigDecimal.ZERO : charge.getAmount();

            if (charge.getChargeType() == ChargeType.PENALTY) {
                dto.setPenaltyAmount(dto.getPenaltyAmount().add(amount));
            } else if (charge.getChargeType() == ChargeType.BONUS) {
                dto.setBonusAmount(dto.getBonusAmount().add(amount));
            }

            dto.setAmount(dto.getPenaltyAmount().add(dto.getBonusAmount()));
            dto.setDaysLate(Math.max(
                    dto.getDaysLate() == null ? 0 : dto.getDaysLate(),
                    charge.getDaysLate() == null ? 0 : charge.getDaysLate()));
        }

        // Dynamically calculate charges for overdue pending EMIs
        LocalDate today = LocalDate.now();

        List<EmiSchedule> overdueEmis =
                emiRepo.findByLoanAccountIdAndStatusAndDueDateLessThanEqualOrderByInstallmentNoAsc(
                        loanAccountId, EmiStatus.PENDING, today);

        for (EmiSchedule emi : overdueEmis) {
            if (emi.getDueDate() == null) {
                continue;
            }

            int daysLate = (int) ChronoUnit.DAYS.between(emi.getDueDate(), today);
            if (daysLate <= 0) {
                continue;
            }

            long monthsLate = ChronoUnit.MONTHS.between(
                    YearMonth.from(emi.getDueDate()), YearMonth.from(today));

            BigDecimal penalty = BigDecimal.valueOf(daysLate);
            BigDecimal bonus = BigDecimal.valueOf(Math.max(0L, monthsLate) * 500L);

            PenaltyResponse dto = result.computeIfAbsent(
                    emi.getEmiScheduleId(), id -> {
                        PenaltyResponse r = new PenaltyResponse();
                        r.setEmiScheduleId(id);
                        r.setPenaltyChargeId(null);
                        r.setChargeDate(today);
                        return r;
                    });

            // Replace with current calculated values, don't add them twice
            dto.setPenaltyAmount(penalty);
            dto.setBonusAmount(bonus);
            dto.setAmount(penalty.add(bonus));
            dto.setDaysLate(daysLate);
        }

        return new ArrayList<>(result.values());
    }

    @Override
    public PenaltyResponse getPenalty(Integer penaltyChargeId) {

        PenaltyCharge charge = penaltyRepo.findById(penaltyChargeId)
                .orElseThrow(() -> new PenaltyNotFoundException(
                        "Penalty charge not found with ID: " + penaltyChargeId));

        return mapper.map(charge, PenaltyResponse.class);
    }
}
