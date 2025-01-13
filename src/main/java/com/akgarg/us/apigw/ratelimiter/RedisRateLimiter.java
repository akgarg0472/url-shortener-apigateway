package com.akgarg.us.apigw.ratelimiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@Profile("prod")
public class RedisRateLimiter extends AbstractRateLimiter {

    private static final String RATE_LIMIT_PREFIX = "rateLimit:";
    private static final long TTL = Duration.ofMinutes(1).toMillis();

    private final RedisTemplate<String, Integer> redisTemplate;

    public RedisRateLimiter(final RedisTemplate<String, Integer> redisTemplate, final Environment environment) {
        super();
        updateAllowedRequestsPerMinute(Objects.requireNonNull(environment, "environment is required"));
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isRateLimited(final String apiRoute, final String requestPath, final String identifier) {
        if (requestPath == null || identifier == null || requestPath.isBlank() || identifier.isBlank()) {
            throw new IllegalArgumentException("Invalid requested path or identifier.");
        }

        final var key = RATE_LIMIT_PREFIX + createKey(requestPath, identifier);
        final var valueOperations = redisTemplate.opsForValue();

        final var currentRequestCount = valueOperations.get(key);

        if (currentRequestCount == null) {
            valueOperations.set(key, 1, TTL, TimeUnit.MILLISECONDS);
            log.trace("First request for key: {}. Rate limiting starts.", key);
            return false;
        }

        final var allowedRequests = AbstractRateLimiter.allowedRequests.get(apiRoute);

        if (allowedRequests == null) {
            return false;
        }

        if (currentRequestCount >= allowedRequests) {
            log.trace("Rate limit exceeded for key: {}. Current count: {}, Allowed: {}", key, currentRequestCount, allowedRequests);
            return true;
        }

        valueOperations.increment(key);

        return false;
    }

}
