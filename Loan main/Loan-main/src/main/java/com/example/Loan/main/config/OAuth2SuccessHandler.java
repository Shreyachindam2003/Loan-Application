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

    private static final String FRONTEND_ORIGIN = "http://localhost:4200";

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

        Map<String, Object> result = new LinkedHashMap<>();

        if (email == null || email.isBlank()) {
            result.put("type", "GOOGLE_LOGIN_ERROR");
            result.put("message", "Google verified email not found");

            sendPopupResult(response, result);
            return;
        }

        try {
            AuthResponse authResponse = authService.oauthLogin(email);

            // Keep the existing authenticator verification mandatory.
            result.put("type", "GOOGLE_LOGIN_2FA_REQUIRED");
            result.put("email", email);
            result.put("twoFactorRequired", authResponse.twoFactorRequired());
            result.put("message", authResponse.message());
            result.put("twoFactorSetupUri", authResponse.twoFactorSetupUri());

            // Do not send access or refresh tokens before 2FA succeeds.
            sendPopupResult(response, result);

        } catch (Exception ex) {
            result.clear();
            result.put("type", "GOOGLE_LOGIN_ERROR");
            result.put("message",
                    "Google login could not be completed. Please try again.");

            sendPopupResult(response, result);
        }
    }

    private void sendPopupResult(
            HttpServletResponse response,
            Map<String, Object> result) throws IOException {

        String json = objectMapper.writeValueAsString(result)
                .replace("<", "\\u003c");

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");

        response.getWriter().write("""
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>LoanApp Google Sign-In</title>
                </head>
                <body>
                    <p>Completing Google sign-in...</p>
                    <script>
                        (function () {
                            const result = %s;

                            if (window.opener && !window.opener.closed) {
                                window.opener.postMessage(
                                    result,
                                    '%s'
                                );
                                window.close();
                            } else {
                                document.body.textContent =
                                    'Google sign-in completed. Return to the LoanApp login page.';
                            }
                        })();
                    </script>
                </body>
                </html>
                """.formatted(json, FRONTEND_ORIGIN));

        response.getWriter().flush();
    }
}