
package com.example.Loan.main.config;

import com.example.Loan.main.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final OAuth2SuccessHandler oauth2SuccessHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter,
            OAuth2SuccessHandler oauth2SuccessHandler) {
        this.jwtFilter = jwtFilter;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
                List.of("http://localhost:4200")
        );

        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        config.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept", "Origin")
        );

        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public health endpoint
                        .requestMatchers(
                                HttpMethod.GET, "/api/v1/health"
                        ).permitAll()

                        // Authentication endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/v1/auth/**"
                        ).permitAll()

                        // Google OAuth2
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        // Customer EMI schedules
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/emi-schedules/my"
                        ).hasRole("USER")

                        // Customer and officer can view closure requests
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/closure/requests"
                        ).hasAnyRole("USER", "LOAN_OFFICER")

                        // Officer-only payment endpoint
                        .requestMatchers(
                                "/api/closure/payment"
                        ).hasRole("LOAN_OFFICER")

                        // Officer-only closure completion
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/closure/complete"
                        ).hasRole("LOAN_OFFICER")

                        // Customer creates closure request
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/closure/request"
                        ).hasRole("USER")

                        // Existing customer endpoint
                        .requestMatchers(
                                "/api/v1/customers/me"
                        ).hasAnyRole("USER", "LOAN_OFFICER")

                        // All other endpoints remain protected
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                        )
                )
                .oauth2Login(oauth ->
                        oauth.successHandler(oauth2SuccessHandler)
                )
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
