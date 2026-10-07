package com.example.loanapplication.service;

import com.example.loanapplication.dto.*;
import com.example.loanapplication.entity.*;
import com.example.loanapplication.repository.*;
import com.example.loanapplication.security.JwtService;
import com.example.loanapplication.security.TotpService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final EmailService emailService;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${app.otp.expiration-minutes}")
    private long otpExpirationMinutes;

    public AuthService(
            CustomerRepository customerRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TotpService totpService,
            EmailService emailService) {

        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.totpService = totpService;
        this.emailService = emailService;
    }

    // =========================
    // CUSTOMER REGISTRATION
    // =========================

    public MessageResponse register(RegisterRequest r) {

        String email = r.email().toLowerCase().trim();

        if (customerRepository.existsByEmail(email)
                || userRepository.existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered");
        }

        if (!r.employmentType().equalsIgnoreCase("Private")
                && !r.employmentType().equalsIgnoreCase("Government")
                && !r.employmentType().equalsIgnoreCase("Self-employed")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employment type must be Private, Government or Self-employed");
        }

        // Salary validation
        if (r.salary().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Salary must be greater than 0");
        }

        // Monthly investment cannot be more than 40% of salary
        BigDecimal maxInvestment =
                r.salary().multiply(new BigDecimal("0.40"));

        if (r.monthlyInvestment().compareTo(maxInvestment) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Registration rejected: monthly investment cannot be more than 40% of salary");
        }

        // Generate OTP
        String otp = String.format(
                "%06d",
                new Random().nextInt(1_000_000));

        // Create Customer
        Customer customer = new Customer();

        customer.setFirstName(r.firstName());
        customer.setLastName(r.lastName());
        customer.setEmail(email);
        customer.setPassword(
                passwordEncoder.encode(r.password()));
        customer.setMobileNo(r.mobileNumber());
        customer.setPanNo(r.panNo());
        customer.setAadhaarNo(r.aadhaarNo());
        customer.setAge(r.age());
        customer.setEmploymentType(r.employmentType());
        customer.setMonthlyIncome(r.salary());
        customer.setMonthlyInvestment(r.monthlyInvestment());

        customer.setEmailVerified(false);

        customer.setVerificationOtpHash(hash(otp));

        customer.setVerificationOtpExpiry(
                LocalDateTime.now()
                        .plusMinutes(otpExpirationMinutes));

        customerRepository.save(customer);

        // Find User role
        Role role = roleRepository.findByRoleName("User")
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "User role not found. Run the SQL script first."));

        // Create User
        User user = new User();

        user.setRole(role);
        user.setCustomer(customer);
        user.setFirstName(r.firstName());
        user.setLastName(r.lastName());
        user.setEmail(email);
        user.setMobile(r.mobileNumber());
        user.setPassword(
                passwordEncoder.encode(r.password()));

        user.setTwoFactorEnabled(false);

        userRepository.save(user);

        // Send OTP to email
        emailService.sendOtpEmail(email, otp);

        return new MessageResponse(
                "Registration successful. OTP sent to your email.");
    }

    // =========================
    // VERIFY EMAIL OTP
    // =========================

    public MessageResponse verifyEmail(
            VerifyOtpRequest request) {

        Customer customer =
                customerRepository
                        .findByEmail(
                                request.email()
                                        .toLowerCase()
                                        .trim())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Customer not found"));

        if (Boolean.TRUE.equals(
                customer.getEmailVerified())) {

            return new MessageResponse(
                    "Email already verified");
        }

        if (customer.getVerificationOtpExpiry() == null
                || customer.getVerificationOtpExpiry()
                .isBefore(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP expired");
        }

        if (!hash(request.otp())
                .equals(customer.getVerificationOtpHash())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid OTP");
        }

        customer.setEmailVerified(true);
        customer.setVerificationOtpHash(null);
        customer.setVerificationOtpExpiry(null);

        customerRepository.save(customer);

        return new MessageResponse(
                "Email verified successfully");
    }

    // =========================
    // RESEND OTP
    // =========================

    public MessageResponse resendOtp(String email) {

        Customer customer =
                customerRepository
                        .findByEmail(
                                email.toLowerCase().trim())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Customer not found"));

        String otp = String.format(
                "%06d",
                new Random().nextInt(1_000_000));

        customer.setVerificationOtpHash(hash(otp));

        customer.setVerificationOtpExpiry(
                LocalDateTime.now()
                        .plusMinutes(otpExpirationMinutes));

        customerRepository.save(customer);

        // Send new OTP to email
        emailService.sendOtpEmail(email, otp);

        return new MessageResponse(
                "OTP resent. Check your email.");
    }

    // =========================
    // LOGIN
    // =========================

    public AuthResponse login(LoginRequest request) {

        User user =
                userRepository
                        .findByEmail(
                                request.email()
                                        .toLowerCase()
                                        .trim())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid email or password"));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        if (user.getCustomer() != null
                && !Boolean.TRUE.equals(
                user.getCustomer().getEmailVerified())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Please verify email first");
        }

        // Check 2FA
        if (Boolean.TRUE.equals(
                user.getTwoFactorEnabled())) {

            if (request.twoFactorCode() == null
                    || !totpService.verifyCode(
                    user.getTwoFactorSecret(),
                    request.twoFactorCode())) {

                return new AuthResponse(
                        null,
                        null,
                        true,
                        "Valid 2FA code required");
            }
        }

        return issueTokens(user);
    }

    // =========================
    // REFRESH TOKEN
    // =========================

    public AuthResponse refresh(String rawRefreshToken) {

        String tokenHash = hash(rawRefreshToken);

        RefreshToken token =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid refresh token"));

        if (token.isRevoked()
                || token.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expired or revoked");
        }

        return new AuthResponse(
                jwtService.generateAccessToken(
                        token.getUser().getEmail()),
                rawRefreshToken,
                false,
                "Token refreshed");
    }

    // =========================
    // LOGOUT
    // =========================

    public MessageResponse logout(
            String refreshToken) {

        refreshTokenRepository
                .findByTokenHash(hash(refreshToken))
                .ifPresent(token -> {

                    token.setRevoked(true);

                    refreshTokenRepository.save(token);
                });

        return new MessageResponse(
                "Logged out successfully");
    }

    // =========================
    // 2FA SETUP
    // =========================

    public String setup2fa(String email) {

        User user = getUser(email);

        String secret =
                totpService.generateSecret();

        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(false);

        userRepository.save(user);

        return totpService.provisioningUri(
                email,
                secret);
    }

    // =========================
    // ENABLE 2FA
    // =========================

    public MessageResponse enable2fa(
            String email,
            String code) {

        User user = getUser(email);

        if (user.getTwoFactorSecret() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Run 2FA setup first");
        }

        if (!totpService.verifyCode(
                user.getTwoFactorSecret(),
                code)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid 2FA code");
        }

        user.setTwoFactorEnabled(true);

        userRepository.save(user);

        return new MessageResponse(
                "2FA enabled successfully");
    }

    // =========================
    // DISABLE 2FA
    // =========================

    public MessageResponse disable2fa(
            String email,
            String code) {

        User user = getUser(email);

        if (user.getTwoFactorSecret() == null
                || !totpService.verifyCode(
                user.getTwoFactorSecret(),
                code)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid 2FA code");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);

        userRepository.save(user);

        return new MessageResponse(
                "2FA disabled successfully");
    }

    // =========================
    // ISSUE JWT TOKENS
    // =========================

    private AuthResponse issueTokens(User user) {

        String access =
                jwtService.generateAccessToken(
                        user.getEmail());

        String refresh =
                UUID.randomUUID().toString();

        RefreshToken entity =
                new RefreshToken();

        entity.setUser(user);

        entity.setTokenHash(
                hash(refresh));

        entity.setExpiresAt(
                LocalDateTime.now()
                        .plusSeconds(
                                refreshExpiration / 1000));

        entity.setRevoked(false);

        refreshTokenRepository.save(entity);

        return new AuthResponse(
                access,
                refresh,
                false,
                "Login successful");
    }

    // =========================
    // GET USER
    // =========================

    private User getUser(String email) {

        return userRepository
                .findByEmail(
                        email.toLowerCase().trim())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"));
    }

    // =========================
    // SHA-256 HASH
    // =========================

    private String hash(String value) {

        try {

            return HexFormat.of().formatHex(
                    MessageDigest
                            .getInstance("SHA-256")
                            .digest(value.getBytes()));

        } catch (Exception e) {

            throw new IllegalStateException(e);
        }
    }
}