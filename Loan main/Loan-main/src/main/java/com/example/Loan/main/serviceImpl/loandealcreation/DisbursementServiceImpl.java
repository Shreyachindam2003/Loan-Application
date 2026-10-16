package com.example.Loan.main.serviceImpl.loandealcreation;

import com.example.Loan.main.config.loandealcreation.ModelMapperConfig;
import com.example.Loan.main.dto.loandealcreation.request.DisbursementRequest;
import com.example.Loan.main.dto.loandealcreation.response.DisbursementDetailsResponse;
import com.example.Loan.main.dto.loandealcreation.response.DisbursementResponse;
import com.example.Loan.main.dto.loandealcreation.response.LoanAccountResponse;
import com.example.Loan.main.dto.loandealcreation.response.PageResponse;
import com.example.Loan.main.entity.loandealcreation.Disbursement;
import com.example.Loan.main.entity.loandealcreation.LoanAccount;
import com.example.Loan.main.entity.loandealcreation.LoanDeal;
import com.example.Loan.main.entity.loandealcreation.SanctionLetter;
import com.example.Loan.main.enums.loandealcreation.LoanApplicationStatus;
import com.example.Loan.main.exception.loandealcreation.BusinessException;
import com.example.Loan.main.exception.loandealcreation.ResourceNotFoundException;
import com.example.Loan.main.respository.loandealcreation.DisbursementRepository;
import com.example.Loan.main.respository.loandealcreation.LoanAccountRepository;
import com.example.Loan.main.respository.loandealcreation.LoanDealRepository;
import com.example.Loan.main.respository.loandealcreation.SanctionLetterRepository;
import com.example.Loan.main.service.loandealcreation.DisbursementService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DisbursementServiceImpl implements DisbursementService {

    private final LoanDealRepository dealRepo;
    private final SanctionLetterRepository sanctionRepo;
    private final DisbursementRepository repo;
    private final LoanAccountRepository accountRepo;
    private final ModelMapper modelMapper;

    @Value("${app.disbursement.simulation-enabled:false}")
    private boolean simulationEnabled;

    @Override
    @Transactional
    public DisbursementResponse disburse(
            Integer dealId,
            DisbursementRequest request
    ) {
        if (!simulationEnabled) {
            throw new BusinessException(
                    "Bank integration is required; simulation is disabled"
            );
        }

        LoanDeal deal = dealRepo.findByIdForUpdate(dealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan application not found"
                ));

        Disbursement existing = repo.findByDealId(dealId)
                .orElse(null);

        if (existing != null) {
            if (!"SUCCESS".equals(existing.getStatus())
                    || !request.bankPartner().name().equals(
                    existing.getBankPartner()
            )) {

                throw new BusinessException(
                        "Existing disbursement status or bank partner does not match"
                );
            }

            LoanAccount account = accountRepo.findByDealId(dealId)
                    .orElseThrow(() -> new BusinessException(
                            "Disbursement exists but loan account is missing"
                    ));

            return response(existing, account);
        }

        if (deal.getStatus() != LoanApplicationStatus.SANCTIONED) {
            throw new BusinessException(
                    "Only sanctioned applications can be disbursed"
            );
        }

        if (accountRepo.findByDealId(dealId).isPresent()) {
            throw new BusinessException(
                    "Loan account exists without a disbursement record"
            );
        }

        SanctionLetter sanction = sanctionRepo.findByDealId(dealId)
                .orElseThrow(() -> new BusinessException(
                        "Generate the sanction letter first"
                ));

        validate(deal, sanction);

        LocalDateTime now = LocalDateTime.now();

        Disbursement disbursement = new Disbursement();

        disbursement.setDealId(dealId);
        disbursement.setDisburseAmount(sanction.getLoanAmount());
        disbursement.setBankPartner(request.bankPartner().name());
        disbursement.setDisbursementDate(now);
        disbursement.setStatus("SUCCESS");

        repo.saveAndFlush(disbursement);

        LoanAccount account = new LoanAccount();

        account.setCustomerId(deal.getCustomerId());
        account.setDealId(dealId);
        account.setLoanAccountNo(String.format(
                Locale.ROOT,
                "LA-%010d",
                dealId
        ));

        account.setLoanAmount(sanction.getLoanAmount());
        account.setOutstandingPrincipal(sanction.getLoanAmount());
        account.setInterestRate(sanction.getInterestRate());
        account.setTenureMonths(sanction.getTenureMonths());
        account.setEmiAmount(sanction.getEmiAmount());
        account.setTotalPaidAmount(new BigDecimal("0.00"));
        account.setLoanStatus("ACTIVE");
        account.setDisbursementDate(now);
        account.setCreatedAt(now);

        accountRepo.saveAndFlush(account);

        deal.setStatus(LoanApplicationStatus.DISBURSED);
        dealRepo.save(deal);

        return response(disbursement, account);
    }

    @Override
    @Transactional(readOnly = true)
    public DisbursementDetailsResponse getDisbursementById(
            Integer disbursementId
    ) {
        Disbursement disbursement = repo.findById(disbursementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found"
                ));

        return details(disbursement);
    }

    @Override
    @Transactional(readOnly = true)
    public DisbursementDetailsResponse getLatestDisbursementByDealId(
            Integer dealId
    ) {
        Disbursement disbursement = repo
                .findFirstByDealIdOrderByDisbursementDateDescDisbursementIdDesc(
                        dealId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Disbursement not found for this application"
                ));

        return details(disbursement);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DisbursementDetailsResponse> getDealDisbursements(
            Integer dealId,
            int page,
            int size
    ) {
        PageRequest pageable = PaginationSupport.page(
                page,
                size,
                "disbursementDate",
                "disbursementId"
        );

        checkDeal(dealId);

        Page<DisbursementDetailsResponse> result = repo
                .findByDealId(dealId, pageable)
                .map(this::details);

        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanAccountResponse getAccount(Integer loanAccountId) {
        LoanAccount account = accountRepo.findById(loanAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found"
                ));

        return accountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanAccountResponse> getCustomerAccounts(
            Integer customerId
    ) {
        return accountRepo.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::accountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanAccountResponse getAccountByDealId(Integer dealId) {
        LoanAccount account = accountRepo.findByDealId(dealId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found for this application"
                ));

        return accountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanAccountResponse getLatestAccountByCustomerId(
            Integer customerId
    ) {
        LoanAccount account = accountRepo
                .findFirstByCustomerIdOrderByCreatedAtDescLoanAccountIdDesc(
                        customerId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer loan account not found"
                ));

        return accountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LoanAccountResponse> getCustomerAccountPage(
            Integer customerId,
            int page,
            int size
    ) {
        PageRequest pageable = PaginationSupport.page(
                page,
                size,
                "createdAt",
                "loanAccountId"
        );

        Page<LoanAccountResponse> result = accountRepo
                .findByCustomerId(customerId, pageable)
                .map(this::accountResponse);

        return PageResponse.from(result);
    }

    private void checkDeal(Integer dealId) {
        if (!dealRepo.existsById(dealId)) {
            throw new ResourceNotFoundException(
                    "Loan application not found"
            );
        }
    }

    private void validate(
            LoanDeal deal,
            SanctionLetter sanction
    ) {
        if (sanction.getLoanAmount() == null
                || sanction.getLoanAmount().signum() <= 0
                || sanction.getInterestRate() == null
                || sanction.getInterestRate().signum() < 0
                || sanction.getTenureMonths() == null
                || sanction.getTenureMonths() <= 0
                || sanction.getEmiAmount() == null
                || sanction.getEmiAmount().signum() <= 0) {

            throw new BusinessException(
                    "Sanctioned loan terms are incomplete"
            );
        }

        if (deal.getApprovedAmount() == null
                || deal.getApprovedAmount().compareTo(
                sanction.getLoanAmount()
        ) != 0
                || deal.getInterestRate() == null
                || deal.getInterestRate().compareTo(
                sanction.getInterestRate()
        ) != 0
                || !sanction.getTenureMonths().equals(
                deal.getTenureMonths()
        )
                || deal.getEmiAmount() == null
                || deal.getEmiAmount().compareTo(
                sanction.getEmiAmount()
        ) != 0) {

            throw new BusinessException(
                    "Approved terms do not match sanctioned terms"
            );
        }

        if (sanction.getCreatedAt() == null
                || sanction.getValidityDays() == null
                || sanction.getValidityDays() <= 0
                || sanction.getCreatedAt()
                .plusDays(sanction.getValidityDays())
                .isBefore(LocalDateTime.now())) {

            throw new BusinessException(
                    "Sanction letter validity is missing or expired"
            );
        }

        if (deal.getBankName() == null
                || deal.getBankName().isBlank()
                || deal.getBankAccountNumber() == null
                || deal.getBankAccountNumber().isBlank()
                || deal.getIfscCode() == null
                || deal.getIfscCode().isBlank()) {

            throw new BusinessException(
                    "Customer bank details are incomplete"
            );
        }
    }


    private LoanAccountResponse accountResponse(LoanAccount account) {
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


    private DisbursementDetailsResponse details(
            Disbursement disbursement
    ) {
        return new DisbursementDetailsResponse(
                disbursement.getDisbursementId(),
                disbursement.getDealId(),
                disbursement.getDisburseAmount(),
                disbursement.getBankPartner(),
                disbursement.getStatus(),
                disbursement.getDisbursementDate()
        );
    }


    private DisbursementResponse response(
            Disbursement disbursement,
            LoanAccount account
    ) {
        return new DisbursementResponse(
                disbursement.getDisbursementId(),
                disbursement.getDealId(),
                disbursement.getDisburseAmount(),
                disbursement.getBankPartner(),
                disbursement.getStatus(),
                simulationEnabled,
                disbursement.getDisbursementDate(),
                accountResponse(account)
        );
    }

}