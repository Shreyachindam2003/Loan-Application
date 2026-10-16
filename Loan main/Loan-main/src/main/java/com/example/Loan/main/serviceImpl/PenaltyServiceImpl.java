
package com.example.Loan.main.serviceImpl;

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

import com.example.Loan.main.dto.response.PenaltyResponse;
import com.example.Loan.main.entity.EmiSchedule;
import com.example.Loan.main.entity.PenaltyCharge;
import com.example.Loan.main.enums.ChargeType;
import com.example.Loan.main.enums.EmiStatus;
import com.example.Loan.main.exception.PenaltyNotFoundException;
import com.example.Loan.main.respository.EmiScheduleRepository;
import com.example.Loan.main.respository.PenaltyChargeRepository;
import com.example.Loan.main.service.PenaltyService;

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
        BigDecimal zero = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        // Existing saved charges
        for (PenaltyCharge charge : penaltyRepo.findByLoanAccountId(loanAccountId)) {

            Integer emiId = charge.getEmiScheduleId();

            PenaltyResponse dto = result.computeIfAbsent(emiId, id -> {
                PenaltyResponse r = mapper.map(charge, PenaltyResponse.class);
                r.setPenaltyAmount(zero);
                r.setBonusAmount(zero);
                return r;
            });

            BigDecimal amount = charge.getAmount() == null
                    ? zero : charge.getAmount();

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

        // Calculate charges for overdue pending EMIs
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

            dto.setPenaltyAmount(penalty);
            dto.setBonusAmount(bonus);
            dto.setAmount(penalty.add(bonus));
            dto.setDaysLate(daysLate);
        }

        log.info("Penalties fetched for loanAccountId={}", loanAccountId);
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
