package com.example.loanapplication.serviceImpl.auth;

import com.example.loanapplication.dto.auth.*;
import com.example.loanapplication.entity.auth.Customer;
import com.example.loanapplication.entity.auth.RefreshToken;
import com.example.loanapplication.entity.auth.Role;
import com.example.loanapplication.entity.auth.User;
import com.example.loanapplication.repository.auth.CustomerRepository;
import com.example.loanapplication.repository.auth.RefreshTokenRepository;
import com.example.loanapplication.repository.auth.RoleRepository;
import com.example.loanapplication.repository.auth.UserRepository;
import com.example.loanapplication.security.auth.JwtService;
import com.example.loanapplication.security.auth.TotpService;
import com.example.loanapplication.service.auth.AuthService;
import com.example.loanapplication.service.auth.EmailService;

import jakarta.transaction.Transactional;

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
public class AuthServiceImpl implements AuthService {

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

    public AuthServiceImpl(
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

    @Override
    @Transactional
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

        if (r.salary().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Salary must be greater than 0");
        }

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

        // =========================
        // CREATE CUSTOMER
        // =========================

        Customer customer = new Customer();

        customer.setFirstName(r.firstName());
        customer.setLastName(r.lastName());
        customer.setAge(r.age());
        customer.setEmail(email);

        customer.setPassword(
                passwordEncoder.encode(r.password()));

        customer.setMobileNo(r.mobileNumber());
        customer.setPanNo(r.panNo());
        customer.setAadhaarNo(r.aadhaarNo());
        customer.setEmploymentType(r.employmentType());
        customer.setMonthlyIncome(r.salary());
        customer.setMonthlyInvestment(r.monthlyInvestment());

        customer.setEmailVerified(false);

        customer.setVerificationOtpHash(
                hash(otp));

        customer.setVerificationOtpExpiry(
                LocalDateTime.now()
                        .plusMinutes(otpExpirationMinutes));

        customerRepository.save(customer);

        // =========================
        // FIND USER ROLE
        // =========================

        Role role = roleRepository.findByRoleName("User")
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "User role not found. Run the SQL script first."));

        // =========================
        // CREATE USER
        // =========================

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

        // =========================
        // SEND OTP EMAIL
        // =========================

        emailService.sendOtpEmail(email, otp);

        return new MessageResponse(
                "Registration successful. OTP sent to your email.");
    }

    // =========================
    // VERIFY EMAIL OTP
    // =========================

    @Override
    @Transactional
    public MessageResponse verifyEmail(
            VerifyOtpRequest request) {

        String email = request.email()
                .toLowerCase()
                .trim();

        Customer customer =
                customerRepository
                        .findByEmail(email)
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

    @Override
    @Transactional
    public MessageResponse resendOtp(String email) {

        email = email.toLowerCase().trim();

        Customer customer =
                customerRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Customer not found"));

        if (Boolean.TRUE.equals(
                customer.getEmailVerified())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is already verified");
        }

        String otp = String.format(
                "%06d",
                new Random().nextInt(1_000_000));

        customer.setVerificationOtpHash(
                hash(otp));

        customer.setVerificationOtpExpiry(
                LocalDateTime.now()
                        .plusMinutes(otpExpirationMinutes));

        customerRepository.save(customer);

        // Send new OTP
        emailService.sendOtpEmail(email, otp);

        return new MessageResponse(
                "OTP resent. Check your email.");
    }

    // =========================
    // LOGIN
    // =========================

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        String email = request.email()
                .toLowerCase()
                .trim();

        User user =
                userRepository
                        .findByEmail(email)
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

        // =========================
        // CHECK 2FA
        // =========================

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

    @Override
    @Transactional
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

    @Override
    @Transactional
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

    @Override
    @Transactional
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

    @Override
    @Transactional
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

    @Override
    @Transactional
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
    // GOOGLE OAUTH LOGIN
    // =========================

    @Override
    @Transactional
    public AuthResponse oauthLogin(String email) {

        User user =
                userRepository
                        .findByEmail(
                                email.toLowerCase().trim())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Google user not found"));

        return issueTokens(user);
    }

    // =========================
    // ISSUE JWT TOKENS
    // =========================

    @Transactional
    @Override
    public AuthResponse issueTokens(User user) {

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