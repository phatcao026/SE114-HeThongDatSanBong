package com.example.backend.service;

import com.example.backend.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BookingLockService {
    private static final String RELEASE_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            end
            return 0
            """;

    private final StringRedisTemplate redisTemplate;
    private final boolean enabled;
    private final String lockPrefix;
    private final Duration lockTtl;
    private final DefaultRedisScript<Long> releaseScript;

    public BookingLockService(StringRedisTemplate redisTemplate,
                              @Value("${app.booking.redis-lock-enabled}") boolean enabled,
                              @Value("${app.booking.redis-lock-prefix}") String lockPrefix,
                              @Value("${app.booking.redis-lock-ttl-seconds}") long lockTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.enabled = enabled;
        this.lockPrefix = lockPrefix;
        this.lockTtl = Duration.ofSeconds(lockTtlSeconds);
        this.releaseScript = new DefaultRedisScript<>(RELEASE_SCRIPT, Long.class);
    }

    public String acquire(Long timeSlotId, LocalDate bookingDate) {
        if (!enabled) {
            return null;
        }

        String token = UUID.randomUUID().toString();
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(key(timeSlotId, bookingDate), token, lockTtl);
            if (Boolean.TRUE.equals(acquired)) {
                return token;
            }
            if (acquired == null) {
                throw new AppException(503, "Booking lock service is unavailable");
            }

            throw new AppException(409, "This time slot is being booked. Please try again.");
        } catch (AppException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AppException(503, "Booking lock service is unavailable");
        }
    }

    public void release(Long timeSlotId, LocalDate bookingDate, String token) {
        if (!enabled || token == null) {
            return;
        }

        try {
            redisTemplate.execute(releaseScript, List.of(key(timeSlotId, bookingDate)), token);
        } catch (RuntimeException ignored) {
            // The lock has a short TTL, so release failures should not break a completed request.
        }
    }

    private String key(Long timeSlotId, LocalDate bookingDate) {
        return lockPrefix + timeSlotId + ":" + bookingDate;
    }
}
