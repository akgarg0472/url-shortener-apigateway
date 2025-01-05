package com.akgarg.us.apigw.ratelimiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@Profile({"prod", "PROD"})
@RequiredArgsConstructor
public class RedisRateLimiter extends AbstractRateLimiterService {

    private static final String RATE_LIMIT_PREFIX = "rateLimit:";
    private static final long TTL = Duration.ofMinutes(1).toMillis();

    private final RedisTemplate<String, Integer> redisTemplate;

    @Override
    public boolean isRateLimited(final String requestedPath, final String identifier) {
        if (requestedPath == null || identifier == null || requestedPath.isBlank() || identifier.isBlank()) {
            throw new IllegalArgumentException("Invalid requested path or identifier.");
        }

        final var key = RATE_LIMIT_PREFIX + createKey(requestedPath, identifier);
        final var valueOperations = redisTemplate.opsForValue();

        final var currentRequestCount = valueOperations.get(key);

        if (currentRequestCount == null) {
            valueOperations.set(key, 1, TTL, TimeUnit.MILLISECONDS);
            log.trace("First request for key: {}. Rate limiting starts.", key);
            return false;
        }

        final var allowedRequests = allowedRequestsPerMinute.get(requestedPath);

        if (currentRequestCount >= allowedRequests) {
            log.trace("Rate limit exceeded for key: {}. Current count: {}, Allowed: {}", key, currentRequestCount, allowedRequests);
            return true;
        }

        valueOperations.increment(key);

        return false;
    }

}
