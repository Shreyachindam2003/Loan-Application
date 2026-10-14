package com.example.Loan.main.config;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS = 60;
    private static final long WINDOW_MILLIS = 60_000L;

    private final ConcurrentHashMap<String, ClientWindow> clients = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientKey) {
        long now = System.currentTimeMillis();
        boolean[] allowed = new boolean[1];

        clients.compute(clientKey, (key, current) -> {
            if (current == null || now - current.windowStart >= WINDOW_MILLIS) {
                allowed[0] = true;
                return new ClientWindow(now, 1);
            }

            if (current.requestCount < MAX_REQUESTS) {
                current.requestCount++;
                allowed[0] = true;
            } else {
                allowed[0] = false;
            }
            return current;
        });

        return allowed[0];
    }

    private static final class ClientWindow {
        private final long windowStart;
        private int requestCount;

        private ClientWindow(long windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}
