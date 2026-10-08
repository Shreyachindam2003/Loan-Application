package com.example.loanapplication.config.auth;

import com.example.loanapplication.dto.auth.AuthResponse;
import com.example.loanapplication.service.auth.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    public OAuth2SuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User user =
                (OAuth2User) authentication.getPrincipal();

        String email = user.getAttribute("email");

        if (email == null || email.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"Google email not found\"}"
            );
            return;
        }

        AuthResponse tokens =
                authService.oauthLogin(email);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        response.getWriter().write(
                "{"
                        + "\"accessToken\":\""
                        + tokens.accessToken()
                        + "\","
                        + "\"refreshToken\":\""
                        + tokens.refreshToken()
                        + "\","
                        + "\"twoFactorRequired\":"
                        + tokens.twoFactorRequired()
                        + ","
                        + "\"message\":\"Google login successful\""
                        + "}"
        );
    }
}