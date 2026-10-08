package com.example.Loan.main.serviceImpl.cibil;

import com.example.Loan.main.dto.cibilDto.CibilEligibiltyResponse;
import com.example.Loan.main.dto.cibilDto.RejectEligibilityRequest;
import com.example.Loan.main.entity.cibilEntity.CibilReports;
import com.example.Loan.main.entity.cibilEntity.Customer;
import com.example.Loan.main.entity.cibilEntity.EligibilityResults;
import com.example.Loan.main.exception.cibilExceptions.CibilReportNotFound;
import com.example.Loan.main.exception.cibilExceptions.EligibilityStatusException;
import com.example.Loan.main.exception.cibilExceptions.Eligibiltynotfound;
import com.example.Loan.main.exception.cibilExceptions.rejectNotEmpty;
import com.example.Loan.main.respository.cibilRespository.CibilRepository;
import com.example.Loan.main.respository.cibilRespository.EligibilityRepository;
import com.example.Loan.main.service.cibilService.EligibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityServiceImpl implements EligibilityService {
    private final CibilRepository cibilRepository;
    private final EligibilityRepository eligibilityRepository;
    private final ModelMapper modelMapper;

    @Override
    public CibilEligibiltyResponse checkEligibility(Customer customer) {
        log.info("Eligibility check started for customer");
        CibilReports cibilReports = cibilRepository.findByCustomer(customer)
                .orElseThrow(() -> {
                    log.warn("CIBIL report not found for customer");
                    return new CibilReportNotFound("CIBIL REPORT NOT FOUND");
                });

        Integer Score = cibilReports.getCibilScore();
        log.info("CIBIL score fetched: {}", Score);

        EligibilityResults results = eligibilityRepository.findByCustomer(customer)
                .orElse(new EligibilityResults());

        results.setCustomer(customer);
        results.setCibilScore(Score);
        BigDecimal eligibleLoanAmount = getEligibleLoanAmount(Score);
        results.setLoanAmount(eligibleLoanAmount);
        log.info("Eligible loan amount calculated: {}", eligibleLoanAmount);

        if (Score >= 800) {
            results.setStatus("APPROVED");
            results.setIsEligible(true);
            results.setRejectionReason(null);
            log.info("Eligibility status: APPROVED");

        } else if (Score >= 600) {
            results.setStatus("PENDING");
            results.setIsEligible(null);
            results.setRejectionReason(null);
            log.info("Eligibility status: PENDING");


        } else {
            results.setStatus("REJECTED");
            results.setIsEligible(false);
            results.setRejectionReason("CIBIL score is below 650");
            log.info("Eligibility status: REJECTED");

        }

       eligibilityRepository.save(results);
        log.info("Eligibility result saved successfully with status: {}", results.getStatus());
        return createResponse(results);
    }

    @Override
    public CibilEligibiltyResponse approvedEligibility(Long eligibilityId) {
        log.info("Approving eligibility for eligibilityId: {}", eligibilityId);

        EligibilityResults results = eligibilityRepository.findById(eligibilityId)
                .orElseThrow(()->{
                    log.warn("Eligibility result not found for id: {}", eligibilityId);
                return new Eligibiltynotfound("Eligibility result not found");
                        });
        if (!"PENDING".equalsIgnoreCase(results.getStatus())) {
            log.warn("Approval not allowed. Eligibility id: {} has status: {}",
                    eligibilityId, results.getStatus());

            throw new EligibilityStatusException(
                    "Only PENDING eligibility can be approved"
            );
        }
        results.setStatus("APPROVED");
        results.setIsEligible(true);
        results.setRejectionReason(null);
        eligibilityRepository.save(results);
        log.info("Eligibility approved successfully for id: {}", eligibilityId);
        return  createResponse(results);

       }

    @Override
    public CibilEligibiltyResponse rejectEligibility(Long eligibilityId, RejectEligibilityRequest request) {
        log.info("Rejecting eligibility for eligibilityId: {}", eligibilityId);
        EligibilityResults results = eligibilityRepository.findById(eligibilityId)
                .orElseThrow(()-> {
                    log.warn("Eligibility results not found for id: {}", eligibilityId);
                    return new Eligibiltynotfound("Eligibility result not found");
                });

        if(request==null||request.getRejectionReason()==null
        ||request.getRejectionReason().trim().isEmpty()){
            log.warn("Rejection failed because rejection reason is empty for id: {}",
                    eligibilityId);
            throw  new rejectNotEmpty("Rejection reason is required");

        }

        if (!"PENDING".equalsIgnoreCase(results.getStatus())) {
            log.warn("Rejection not allowed. Eligibility id: {} has status: {}",
                    eligibilityId, results.getStatus());

            throw new EligibilityStatusException(
                    "Only PENDING eligibility can be rejected"
            );
        }
        results.setStatus("REJECTED");
        results.setIsEligible(false);
        results.setRejectionReason(request.getRejectionReason());
        eligibilityRepository.save(results);
        log.info("Eligibility rejected successfully for id: {}", eligibilityId);
        return createResponse(results);

    }

    @Override
    public List<CibilEligibiltyResponse> getPendingEligibilities() {
        log.info("Fetching pending eligibility records for officer");

        List<EligibilityResults> results = eligibilityRepository.findByStatus("PENDING");
        log.info("Pending eligibility count: {}", results.size());
        return results.stream()
                .map(this::createResponse).
                toList();
    }

    private BigDecimal getEligibleLoanAmount(Integer score) {

        if (score >= 900) {
            return new BigDecimal("50000000");

        } else if (score >= 800) {
            return new BigDecimal("10000000");

        } else if (score >= 750) {
            return new BigDecimal("7500000");

        } else if (score >= 700) {
            return new BigDecimal("5000000");

        } else if (score >= 650) {
            return new BigDecimal("2500000");

        } else {
            return BigDecimal.ZERO;
        }
    }


    private CibilEligibiltyResponse createResponse(EligibilityResults results){

        CibilEligibiltyResponse response=modelMapper.map(results, CibilEligibiltyResponse.class);
       Customer customer=results.getCustomer();
       response.setFullName(customer.getFirstName()+ " "+customer.getLastName());
      response.setPanNo(maskPan(customer.getPanNo()));
      response.setMonthlyIncome(customer.getMonthlyIncome());
        log.debug("Eligibility response created successfully");

      return  response;
    }
    private String maskPan(String pan) {
        if (pan == null || pan.length() != 10) {
            return pan;
        }
        return pan.substring(0, 5)
                + "XXXX"
                + pan.substring(9);
    }
}