package com.example.loanapplication.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "FirstName", nullable = false)
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Password")
    private String password;

    @Column(name = "MobileNo")
    private String mobileNo;

    @Column(name = "AadhaarNo")
    private String aadhaarNo;

    @Column(name = "PanNo")
    private String panNo;

    @Column(name = "Age")
    private Integer age;

    @Column(name = "EmploymentType")
    private String employmentType;

    @Column(name = "MonthlyIncome")
    private BigDecimal monthlyIncome;

    @Column(name = "MonthlyInvestment")
    private BigDecimal monthlyInvestment;

    @Column(name = "IsEmailVerified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "VerificationOtpHash")
    private String verificationOtpHash;

    @Column(name = "VerificationOtpExpiry")
    private LocalDateTime verificationOtpExpiry;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        if (emailVerified == null) emailVerified = false;
    }

    public Integer getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getMobileNo() { return mobileNo; }
    public String getAadhaarNo() { return aadhaarNo; }
    public String getPanNo() { return panNo; }
    public Integer getAge() { return age; }
    public String getEmploymentType() { return employmentType; }
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public BigDecimal getMonthlyInvestment() { return monthlyInvestment; }
    public Boolean getEmailVerified() { return emailVerified; }
    public String getVerificationOtpHash() { return verificationOtpHash; }
    public LocalDateTime getVerificationOtpExpiry() { return verificationOtpExpiry; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }
    public void setAadhaarNo(String aadhaarNo) { this.aadhaarNo = aadhaarNo; }
    public void setPanNo(String panNo) { this.panNo = panNo; }
    public void setAge(Integer age) { this.age = age; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public void setMonthlyInvestment(BigDecimal monthlyInvestment) { this.monthlyInvestment = monthlyInvestment; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public void setVerificationOtpHash(String verificationOtpHash) { this.verificationOtpHash = verificationOtpHash; }
    public void setVerificationOtpExpiry(LocalDateTime verificationOtpExpiry) { this.verificationOtpExpiry = verificationOtpExpiry; }
}
