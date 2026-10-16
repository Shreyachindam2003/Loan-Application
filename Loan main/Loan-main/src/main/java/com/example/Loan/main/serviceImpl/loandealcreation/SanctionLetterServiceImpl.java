package com.example.Loan.main.serviceImpl.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.SanctionLetterResponse;
import com.example.Loan.main.entity.loandealcreation.LoanDeal;
import com.example.Loan.main.entity.loandealcreation.SanctionLetter;
import com.example.Loan.main.enums.loandealcreation.LoanApplicationStatus;
import com.example.Loan.main.enums.loandealcreation.LoanType;
import com.example.Loan.main.exception.loandealcreation.BusinessException;
import com.example.Loan.main.exception.loandealcreation.ResourceNotFoundException;
import com.example.Loan.main.respository.loandealcreation.LoanDealRepository;
import com.example.Loan.main.respository.loandealcreation.SanctionLetterRepository;
import com.example.Loan.main.service.loandealcreation.SanctionLetterService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanctionLetterServiceImpl
        implements SanctionLetterService {

    private final SanctionLetterRepository repo;
    private final LoanDealRepository dealRepo;
    private final JdbcTemplate jdbc;
    private final ModelMapper modelMapper;

    @Value("${app.sanction.processing-charge-percent}")
    private BigDecimal processingPercent;

    @Value("${app.sanction.origination-fee}")
    private BigDecimal originationFee;

    @Value("${app.sanction.validity-days}")
    private int validityDays;

    @Value("${app.sanction.issuing-bank}")
    private String issuingBank;

    @Value("${app.sanction.conditions}")
    private String conditions;

    @Value("${app.sanction.authorized-signatory}")
    private String signatory;

    @Override
    @Transactional
    public SanctionLetterResponse generate(Integer dealId) {
        LoanDeal deal = dealRepo.findByIdForUpdate(dealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan application not found"
                ));

        SanctionLetter existing = repo.findByDealId(dealId)
                .orElse(null);

        if (existing != null) {
            if (deal.getStatus() == LoanApplicationStatus.APPROVED) {
                deal.setStatus(LoanApplicationStatus.SANCTIONED);
                dealRepo.save(deal);
            }

            return response(existing);
        }

        if (deal.getStatus() != LoanApplicationStatus.APPROVED) {
            throw new BusinessException(
                    "Sanction letter requires an approved application"
            );
        }

        if (deal.getApprovedAmount() == null
                || deal.getApprovedAmount().signum() <= 0
                || deal.getInterestRate() == null
                || deal.getTenureMonths() == null
                || deal.getTenureMonths() <= 0
                || deal.getEmiAmount() == null
                || deal.getEmiAmount().signum() <= 0
                || deal.getLoanType() == null) {

            throw new BusinessException(
                    "Approved loan terms are incomplete"
            );
        }

        if (processingPercent.signum() < 0
                || originationFee.signum() < 0
                || validityDays <= 0) {

            throw new BusinessException(
                    "Sanction policy configuration is invalid"
            );
        }

        if (deal.getLoanType() == LoanType.HOME
                && (deal.getPropertyAddress() == null
                || deal.getPropertyAddress().isBlank())) {

            throw new BusinessException(
                    "Property address is required for a HOME loan"
            );
        }

        List<Applicant> applicants = jdbc.query("""
                SELECT FirstName, LastName, MobileNo
                FROM Customers
                WHERE CustomerId = ?
                """,
                (rs, row) -> new Applicant(
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("MobileNo")
                ),
                deal.getCustomerId()
        );

        if (applicants.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Customer not found"
            );
        }

        Applicant applicant = applicants.get(0);

        String name = (
                (applicant.firstName() == null
                        ? "" : applicant.firstName())
                        + " "
                        + (applicant.lastName() == null
                        ? "" : applicant.lastName())
        ).trim();

        SanctionLetter letter = new SanctionLetter();

        letter.setDealId(dealId);
        letter.setLoanAmount(deal.getApprovedAmount());
        letter.setInterestRate(deal.getInterestRate());
        letter.setTenureMonths(deal.getTenureMonths());
        letter.setEmiAmount(deal.getEmiAmount());
        letter.setCreatedAt(LocalDateTime.now());
        letter.setApplicantName(name);
        letter.setMobileNo(applicant.mobileNo());
        letter.setLoanType(deal.getLoanType().name());
        letter.setPropertyAddress(deal.getPropertyAddress());
        letter.setProcessingChargePercent(processingPercent);

        letter.setProcessingChargeAmount(
                deal.getApprovedAmount()
                        .multiply(processingPercent)
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        )
        );

        letter.setOriginationFee(
                originationFee.setScale(2, RoundingMode.HALF_UP)
        );
        letter.setValidityDays(validityDays);
        letter.setIssuingBank(issuingBank);
        letter.setAdditionalConditions(conditions);
        letter.setAuthorizedSignatory(signatory);

        SanctionLetter savedLetter = repo.save(letter);

        deal.setStatus(LoanApplicationStatus.SANCTIONED);
        dealRepo.save(deal);

        return response(savedLetter);
    }

    @Override
    @Transactional(readOnly = true)
    public SanctionLetterResponse getById(Integer sanctionId) {
        return response(findLetter(sanctionId));
    }

    @Override
    @Transactional(readOnly = true)
    public SanctionLetterResponse getByDealId(Integer dealId) {
        SanctionLetter letter = repo.findByDealId(dealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sanction letter not found for this application"
                ));

        return response(letter);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadPdf(Integer sanctionId) {
        SanctionLetterResponse letter = response(
                findLetter(sanctionId)
        );

        List<String> lines = new ArrayList<>();

        lines.add("LOAN SANCTION LETTER");
        lines.add("Sanction Number: " + letter.sanctionNumber());
        lines.add("Application Number: " + letter.applicationNumber());
        lines.add("Applicant: " + value(letter.applicantName()));
        lines.add("Mobile: " + value(letter.mobileNo()));
        lines.add("Loan Type: " + value(letter.loanType()));
        lines.add("Sanctioned Amount: INR "
                + value(letter.sanctionedAmount()));
        lines.add("Annual Interest: "
                + value(letter.annualInterestRate()) + "%");
        lines.add("Tenure: "
                + value(letter.tenureMonths()) + " months");
        lines.add("Monthly EMI: INR " + value(letter.emiAmount()));
        lines.add("Processing Charge: "
                + value(letter.processingChargePercent()) + "%");
        lines.add("Processing Amount: INR "
                + value(letter.processingChargeAmount()));
        lines.add("Origination Fee: INR "
                + value(letter.originationFee()));
        lines.add("Issued At: " + value(letter.issuedAt()));
        lines.add("Validity: "
                + value(letter.validityDays()) + " days");
        lines.add("Valid Until: " + value(letter.validUntil()));
        lines.add("Property Address: " + value(letter.propertyAddress()));
        lines.add("Issuing Bank: " + value(letter.issuingBank()));
        lines.add("Additional Conditions:");

        for (String condition : letter.additionalConditions()) {
            lines.add("- " + condition);
        }

        lines.add("Authorized Signatory: "
                + value(letter.authorizedSignatory()));
        lines.add("Disbursement is a separate step.");
        lines.add("This letter does not confirm transfer of funds.");

        return createPdf(lines);
    }

    private SanctionLetter findLetter(Integer sanctionId) {
        return repo.findById(sanctionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sanction letter not found"
                ));
    }

    private SanctionLetterResponse response(SanctionLetter letter) {

        LocalDateTime issuedAt = letter.getCreatedAt();

        LocalDateTime validUntil =
                issuedAt != null && letter.getValidityDays() != null
                        ? issuedAt.plusDays(letter.getValidityDays())
                        : null;

        List<String> additionalConditions =
                letter.getAdditionalConditions() == null
                        || letter.getAdditionalConditions().isBlank()
                        ? List.of()
                        : letter.getAdditionalConditions()
                        .lines()
                        .map(String::trim)
                        .filter(condition -> !condition.isBlank())
                        .toList();

        return new SanctionLetterResponse(
                letter.getSanctionId(),
                "SAN-" + String.format("%08d", letter.getSanctionId()),
                letter.getDealId(),
                "APP-" + letter.getDealId(),
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
                issuedAt,
                validUntil,
                letter.getPropertyAddress(),
                letter.getIssuingBank(),
                additionalConditions,
                letter.getAuthorizedSignatory()
        );
    }

    private String value(Object value) {
        return value == null ? "Not recorded" : value.toString();
    }

    private byte[] createPdf(List<String> details) {
        List<String> lines = new ArrayList<>();

        for (String detail : details) {
            String text = detail.replaceAll("[^\\x20-\\x7E]", "?");

            while (text.length() > 80) {
                int end = text.lastIndexOf(' ', 80);

                if (end <= 0) {
                    end = 80;
                }

                lines.add(text.substring(0, end));
                text = text.substring(end).stripLeading();
            }

            lines.add(text);
        }

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            PDType1Font font = new PDType1Font(
                    Standard14Fonts.FontName.COURIER
            );

            for (int start = 0; start < lines.size(); start += 38) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream content =
                             new PDPageContentStream(document, page)) {

                    content.beginText();
                    content.setFont(font, 10);
                    content.setLeading(19);
                    content.newLineAtOffset(45, 790);

                    int end = Math.min(start + 38, lines.size());

                    for (int index = start; index < end; index++) {
                        content.showText(lines.get(index));
                        content.newLine();
                    }

                    content.endText();
                }
            }

            document.save(output);
            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not generate sanction PDF",
                    exception
            );
        }
    }

    private record Applicant(
            String firstName,
            String lastName,
            String mobileNo
    ) {
    }
}