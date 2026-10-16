package com.example.Loan.main.config;


import com.example.Loan.main.dto.AuthResponse;
import com.example.Loan.main.service.AuthService;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public OAuth2SuccessHandler(
            AuthService authService,
            ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User googleUser =
                (OAuth2User) authentication.getPrincipal();

        String email = googleUser.getAttribute("email");

        // Validate Google email
        if (email == null || email.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            objectMapper.writeValue(
                    response.getWriter(),
                    Map.of("message", "Google verified email not found")
            );
            return;
        }

        // Process OAuth login
        AuthResponse result = authService.oauthLogin(email);

        // Prepare response
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("accessToken", result.accessToken());
        body.put("refreshToken", result.refreshToken());
        body.put("twoFactorRequired", result.twoFactorRequired());
        body.put("message", result.message());
        body.put("twoFactorSetupUri", result.twoFactorSetupUri());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), body);
    }
}
