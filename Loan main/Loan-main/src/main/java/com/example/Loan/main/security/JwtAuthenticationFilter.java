
package com.example.Loan.main.security;


import com.example.Loan.main.respository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String header = request.getHeader("Authorization");

        try {

            if (header == null || header.isBlank()) {
                log.debug("No Authorization header for {}", path);

                filterChain.doFilter(request, response);
                return;
            }

            if (!header.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for {}", path);

                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7).trim();

            if (token.isBlank()) {
                log.warn("Empty Bearer token for {}", path);

                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtService.isValid(token)) {
                log.warn("JWT validation failed for {}", path);

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtService.extractEmail(token);

            if (email == null || email.isBlank()) {
                log.warn("JWT has no valid email subject for {}", path);

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            var optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty()) {
                log.warn("JWT user was not found for {}", path);

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            var user = optionalUser.get();

            String role = user.getRole() == null
                    ? "USER"
                    : user.getRole().getRoleName();

            if (role == null || role.isBlank()) {
                role = "USER";
            }

            role = role.trim()
                    .toUpperCase(Locale.ROOT)
                    .replace(' ', '_');

            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }

            String authority = "ROLE_" + role;

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(authority))
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            log.debug(
                    "JWT authenticated request {} with authority {}",
                    path,
                    authority
            );

            filterChain.doFilter(request, response);

        } catch (Exception ex) {

            SecurityContextHolder.clearContext();

            // Do not log the token or sensitive authentication data.
            log.error(
                    "JWT authentication processing failed for {}: {}",
                    path,
                    ex.getMessage()
            );

            // Continue the security chain unauthenticated.
            // Protected endpoints will be rejected by Spring Security.
            filterChain.doFilter(request, response);
        }
    }
}
