package com.example.Loan.main.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private int count = 0;
    private long startTime = System.currentTimeMillis();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        long currentTime = System.currentTimeMillis();

        if (currentTime-startTime>=60000) {
            count =0;
            startTime= currentTime;
        }
        count++;

        if (count > 60) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"Too many requests. Try again later.\"}"
            );
            return;
        }
        filterChain.doFilter(request, response);
    }
}