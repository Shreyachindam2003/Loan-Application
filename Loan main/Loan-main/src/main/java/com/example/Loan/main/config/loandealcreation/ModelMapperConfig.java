package com.example.Loan.main.config.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.LoanApplyRequest;
import com.example.Loan.main.dto.loandealcreation.response.*;
import com.example.Loan.main.entity.loandealcreation.*;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration()
                .setImplicitMappingEnabled(false);

        mapper.createTypeMap(
                LoanApplyRequest.class,
                LoanDeal.class
        ).setConverter(context ->
                mapRequest(context.getSource())
        );

        mapper.createTypeMap(
                ScoreCard.class,
                ScoreCardResponse.class
        ).setConverter(context ->
                mapScoreCard(context.getSource())
        );

        mapper.createTypeMap(
                LoanDeal.class,
                LoanDealResponse.class
        ).setConverter(context ->
                mapLoanDeal(context.getSource())
        );

        mapper.createTypeMap(
                SanctionLetter.class,
                SanctionLetterResponse.class
        ).setConverter(context ->
                mapSanction(context.getSource())
        );

        mapper.createTypeMap(
                LoanAccount.class,
                LoanAccountResponse.class
        ).setConverter(context ->
                mapAccount(context.getSource())
        );

        mapper.createTypeMap(
                DisbursementData.class,
                DisbursementResponse.class
        ).setConverter(context ->
                mapDisbursement(context.getSource())
        );

        return mapper;
    }

    private LoanDeal mapRequest(LoanApplyRequest request) {
        LoanDeal deal = new LoanDeal();

        deal.setScoreCardId(request.scoreCardId());
        deal.setLoanType(request.loanType());
        deal.setLoanAmount(request.loanAmount());
        deal.setTenureMonths(request.tenureMonths());
        deal.setBankName(request.bankName().trim());
        deal.setBankAccountNumber(request.bankAccountNumber());
        deal.setIfscCode(request.ifscCode());
        deal.setRequestedDisbursementDate(
                request.requestedDisbursementDate()
        );
        deal.setEmiDay(request.emiDay());

        String address = request.propertyAddress();

        deal.setPropertyAddress(
                address == null ? null : address.trim()
        );

        return deal;
    }

    private ScoreCardResponse mapScoreCard(ScoreCard card) {
        return new ScoreCardResponse(
                card.getScoreCardId(),
                card.getCustomerId(),
                card.getRiskCategory(),
                card.getEligibleLoanAmount(),
                card.getCibilScore(),
                card.getCurrentStatus(),
                card.getAppliedDate()
        );
    }

    private LoanDealResponse mapLoanDeal(LoanDeal deal) {
        return new LoanDealResponse(
                deal.getDealId(),
                deal.getCustomerId(),
                deal.getScoreCardId(),
                deal.getLoanType(),
                deal.getLoanAmount(),
                deal.getEligibleAmountAtApplication(),
                deal.getInterestRate(),
                deal.getTenureMonths(),
                deal.getEmiAmount(),
                deal.getRequestedDisbursementDate(),
                deal.getEmiDay(),
                deal.getStatus(),
                deal.getCreatedAt()
        );
    }

    private SanctionLetterResponse mapSanction(
            SanctionLetter letter
    ) {
        List<String> conditions =
                letter.getAdditionalConditions() == null
                        ? List.of()
                        : Arrays.stream(
                                letter.getAdditionalConditions()
                                        .split(";")
                        )
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .toList();

        LocalDateTime validUntil = null;

        if (letter.getCreatedAt() != null
                && letter.getValidityDays() != null) {

            validUntil = letter.getCreatedAt()
                    .plusDays(letter.getValidityDays());
        }

        String sanctionNumber = String.format(
                Locale.ROOT,
                "SAN-%08d",
                letter.getSanctionId()
        );

        String applicationNumber = String.format(
                Locale.ROOT,
                "APP%06d",
                letter.getDealId()
        );

        return new SanctionLetterResponse(
                letter.getSanctionId(),
                sanctionNumber,
                letter.getDealId(),
                applicationNumber,
                letter.getApplicantName(),
                letter.getMobileNo(),
                letter.getLoanType(),
                letter.getLoanAmount(),
                letter.getInterestRate(),
                letter.getTenureMonths(),
                letter.getEmiAmount(),
                letter.getProcessingChargePercent(),
                letter.getProcessingChargeAmount(),
                letter.getOriginationFee(),
                letter.getValidityDays(),
                letter.getCreatedAt(),
                validUntil,
                letter.getPropertyAddress(),
                letter.getIssuingBank(),
                conditions,
                letter.getAuthorizedSignatory()
        );
    }

    private LoanAccountResponse mapAccount(
            LoanAccount account
    ) {
        return new LoanAccountResponse(
                account.getLoanAccountId(),
                account.getLoanAccountNo(),
                account.getCustomerId(),
                account.getDealId(),
                account.getLoanAmount(),
                account.getOutstandingPrincipal(),
                account.getInterestRate(),
                account.getTenureMonths(),
                account.getEmiAmount(),
                account.getTotalPaidAmount(),
                account.getLoanStatus(),
                account.getDisbursementDate()
        );
    }

    private DisbursementResponse mapDisbursement(
            DisbursementData data
    ) {
        Disbursement disbursement = data.disbursement();

        return new DisbursementResponse(
                disbursement.getDisbursementId(),
                disbursement.getDealId(),
                disbursement.getDisburseAmount(),
                disbursement.getBankPartner(),
                disbursement.getStatus(),
                true,
                disbursement.getDisbursementDate(),
                mapAccount(data.account())
        );
    }

    public record DisbursementData(
            Disbursement disbursement,
            LoanAccount account
    ) {
    }
}