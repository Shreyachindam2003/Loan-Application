package com.example.Loan.main.serviceImpl.cibil;

import com.example.Loan.main.dto.cibilDto.CibilResponse;
import com.example.Loan.main.dto.cibilDto.CustomerCibilResponse;
import com.example.Loan.main.entity.cibilEntity.CibilReports;
import com.example.Loan.main.entity.cibilEntity.Customer;
import com.example.Loan.main.respository.cibilRespository.CibilRepository;
import com.example.Loan.main.service.cibilService.CibilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CibilServiceImpl implements CibilService {

    private final CibilRepository cibilRepository;
    private final ModelMapper modelMapper;

    private String maskPan(String pan) {
        if (pan == null || pan.length() != 10) {
            return pan;
        }
        return pan.substring(0, 5)
                + "xxxx"
                + pan.substring(9);
    }


    @Override
    public CibilResponse calculateCibil(Customer customers) {

        log.info("Starting CIBIL calculation for customerId: {}",
                customers.getCustomerId());
        Integer incomeScore = calculateIncomeScore(customers.getMonthlyIncome());
        Integer employeeScore = calculateEmployeeScore(customers.getEmploymentType());
        Integer ageScore = calculateAgeScore(customers.getAge());
        Integer FoirScore = calculateFOIRScore (customers.getMonthlyInvestment(),customers.getMonthlyIncome());

        log.debug("CIBIL score components - incomeScore: {}, employeeScore: {}, " +
                        "ageScore: {}, foirScore: {}",
                incomeScore, employeeScore, ageScore, FoirScore);

        Integer totalScore = incomeScore + employeeScore + ageScore + FoirScore;
        log.info("Total CIBIL score calculated: {} for customerId: {}", totalScore, customers.getCustomerId());


        CibilReports reports = cibilRepository.findByCustomer(customers)
                .orElse(new CibilReports());
        boolean existingReport = reports.getCibilReportId()!= null;


        if (existingReport) {
            log.info("Existing CIBIL report found. Updating reportId: {}",
                    reports.getCibilReportId());
        } else {
            log.info("No existing CIBIL report found. Creating new report");
        }

        reports.setCustomer(customers);
        reports.setPanNo(customers.getPanNo());
        reports.setCheckDate(LocalDateTime.now());
        reports.setCibilScore(totalScore);
        reports.setStatus(statusScore(totalScore));
        cibilRepository.save(reports);
        log.info("CIBIL report saved successfully. Score: {}, Status: {}", totalScore, reports.getStatus());
        return getCibilScore(reports);
    }

    @Override
    public CustomerCibilResponse getCustomerDetails(Customer customers) {


        log.debug("Fetching customer details for customerId: {}",
                customers.getCustomerId());
        CustomerCibilResponse response = modelMapper.map(customers, CustomerCibilResponse.class);
        response.setFullName(customers.getFirstName() + " " + customers.getLastName());
        response.setPanNumber(maskPan(customers.getPanNo()));
        log.debug("Customer details mapped successfully for customerId: {}",
                customers.getCustomerId());

        return response;
    }

    @Override
    public CibilResponse getCibilScore(CibilReports cibilReports) {
        log.debug("Mapping CIBIL report to response. Score: {}, Status: {}",
                cibilReports.getCibilScore(),
                cibilReports.getStatus());
        return modelMapper.map(cibilReports, CibilResponse.class);
    }


    private Integer calculateIncomeScore(BigDecimal monthlyIncome) {
        if (monthlyIncome == null) {
            return 0;
        } else if (monthlyIncome.compareTo(new BigDecimal("25000"))<=0) {
            return 100;
        }
        else if (monthlyIncome.compareTo(new BigDecimal("50000")) <= 0) {
            return 200;

        } else if ( monthlyIncome.compareTo(new BigDecimal("100000")) <= 0) {
            return 300;

        } else if (monthlyIncome.compareTo(new BigDecimal("100000")) > 0) {
            return 400;

        } else {
            return 0;
        }
    }




    private Integer calculateEmployeeScore(String employeeType) {
        if (employeeType == null) {
            return 0;
        } else if (employeeType.equalsIgnoreCase("Government")) {
            return 200;

        } else if (employeeType.equalsIgnoreCase("Private")) {
            return 150;

        } else if (employeeType.equalsIgnoreCase("Self-employed")) {
            return 100;

        } else {
            return 0;
        }
    }


    private Integer calculateAgeScore(Integer age) {
        if (age == null) {
            return 0;
        } else if (age >= 21 && age <= 24) {
            return 50;

        } else if (age >= 25 && age <= 45) {
            return 150;
        } else if (age >= 46 && age <= 60) {
            return 100;
        } else {
            return 0;
        }
    }

    private Integer calculateFOIRScore(BigDecimal monthlyInvestment, BigDecimal monthlyIncome) {


        if (monthlyInvestment == null || monthlyIncome == null
                || monthlyIncome.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        BigDecimal foir = monthlyInvestment
                .divide(monthlyIncome, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        if (foir.compareTo(new BigDecimal("35")) <= 0) {
            return 250;

        } else if (foir.compareTo(new BigDecimal("50")) <= 0) {
            return 150;

        } else if (foir.compareTo(new BigDecimal("60")) <= 0) {
            return 75;

        } else {
            return 0;
        }
    }
    private String statusScore(Integer score) {

        if (score >= 900) {
            return "EXCELLENT";
        } else if (score >= 800 && score <= 899) {
            return "VERY GOOD";

        } else if (score >= 750 && score <= 799) {
            return "GOOD";
        }
        else if (score >= 700 && score <= 749) {
            return "AVERAGE";
        }


        else if (score >= 650 && score <= 699) {
            return "RISKY";

        } else {
            return "REJECT";
        }


    }
}