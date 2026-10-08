package com.example.loanapplication.controller.auth;

import com.example.loanapplication.dto.auth.*;
import com.example.loanapplication.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        return service.verifyEmail(request);
    }

    @PostMapping("/resend-otp")
    public MessageResponse resendOtp(@RequestParam String email) {
        return service.resendOtp(email);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return service.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public MessageResponse logout(@Valid @RequestBody RefreshRequest request) {
        return service.logout(request.refreshToken());
    }

    @PostMapping("/2fa/setup")
    public Map<String, String> setup2fa(Authentication authentication) {
        String uri = service.setup2fa(authentication.getName());
        return Map.of("message", "Add this OTP URI to Google Authenticator", "otpauthUri", uri);
    }

    @PostMapping("/2fa/enable")
    public MessageResponse enable2fa(Authentication authentication,
                                     @Valid @RequestBody TwoFactorVerifyRequest request) {
        return service.enable2fa(authentication.getName(), request.code());
    }

    @PostMapping("/2fa/disable")
    public MessageResponse disable2fa(Authentication authentication,
                                      @Valid @RequestBody TwoFactorVerifyRequest request) {
        return service.disable2fa(authentication.getName(), request.code());
    }
}
