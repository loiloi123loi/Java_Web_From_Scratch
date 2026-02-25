package com.polime.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {
    private static final Map<String, RequestCounter> clients = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    public static boolean isAllowed(String ip) {
        long now = System.currentTimeMillis() / 60000;
        RequestCounter counter = clients.computeIfAbsent(ip, k -> new RequestCounter(now));

        if (counter.minute != now) {
            counter.minute = now;
            counter.count.set(1);
            return true;
        }

        return counter.count.incrementAndGet() <= MAX_REQUESTS_PER_MINUTE;
    }

    private static class RequestCounter {
        volatile long minute;
        final AtomicInteger count = new AtomicInteger(0);

        RequestCounter(long minute) {
            this.minute = minute;
        }
    }
}
