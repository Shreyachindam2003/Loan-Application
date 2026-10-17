
package com.example.Loan.main.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 1;
    private static final long TIME_WINDOW = 60;

    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        long currentTime = Instant.now().getEpochSecond();

        RequestInfo info = requestCounts.compute(clientIp, (key, oldInfo) -> {
            if (oldInfo == null || currentTime - oldInfo.startTime >= TIME_WINDOW) {
                return new RequestInfo(currentTime, 1);
            }
            oldInfo.count++;
            return oldInfo;
        });

        if (info.count > MAX_REQUESTS) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Try again later.\",\"data\":null}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static class RequestInfo {
        private final long startTime;
        private int count;

        private RequestInfo(long startTime, int count) {
            this.startTime = startTime;
            this.count = count;
        }
    }
}
