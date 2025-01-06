package com.akgarg.us.apigw.ratelimiter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@code InMemoryRateLimiter} provides a simple in-memory rate-limiting implementation.
 * It limits the number of requests that can be made by a specific client to a particular API endpoint.
 * <p>
 * The rate limiter uses two maps: one to track the request count and the other to track the last request timestamp.
 * It also uses a scheduled task to periodically evict expired entries from the maps to ensure efficient memory usage.
 */
@Service
@Slf4j
@Profile({"dev", "DEV"})
public class InMemoryRateLimiter extends AbstractRateLimiterService {

    /**
     * The interval at which the eviction task runs to remove expired entries, in milliseconds.
     */
    private static final long EVICTION_INTERVAL_MS = 30 * 1000L;

    /**
     * The time-to-live period for each rate-limited entry, in milliseconds.
     * Entries older than this value are considered expired and will be removed.
     */
    private static final long TTL_PERIOD_MS = 60 * 1000L;

    /**
     * Executor for running the eviction task at fixed intervals.
     */
    private final ScheduledExecutorService evictionExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * A map that stores the count of requests for each client (identified by the combination of path and identifier).
     */
    private final Map<String, Integer> requestCountMap = new HashMap<>();

    /**
     * A map that stores the timestamp of the last request for each client.
     */
    private final Map<String, Long> timestampMap = new HashMap<>();

    public InMemoryRateLimiter(final Environment environment) {
        super();
        updateAllowedRequestsPerMinute(Objects.requireNonNull(environment, "environment is required"));
    }

    /**
     * Starts the eviction task after the bean is initialized.
     * The eviction task will run periodically to remove expired entries from the maps.
     */
    @PostConstruct
    public void startEvictionTask() {
        evictionExecutor.scheduleAtFixedRate(this::evictExpiredEntries,
                EVICTION_INTERVAL_MS, EVICTION_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isRateLimited(final String apiRoute, final String requestPath, final String identifier) {
        if (apiRoute == null || apiRoute.isBlank()) {
            throw new IllegalArgumentException("Invalid apiRoute provided");
        }

        if (requestPath == null || identifier == null || requestPath.isBlank() || identifier.isBlank()) {
            throw new IllegalArgumentException("Invalid requested path or identifier.");
        }

        final var allowedRequests = AbstractRateLimiterService.allowedRequests.get(apiRoute);

        if (allowedRequests == null) {
            return false;
        }

        final var key = createKey(requestPath, identifier);
        final var lastRequestTime = timestampMap.getOrDefault(key, 0L);

        if (System.currentTimeMillis() - lastRequestTime > TTL_PERIOD_MS) {
            requestCountMap.put(key, 0);
            timestampMap.put(key, System.currentTimeMillis());
        }

        if (requestCountMap.getOrDefault(key, 0) >= allowedRequests) {
            return true;
        } else {
            requestCountMap.put(key, requestCountMap.getOrDefault(key, 0) + 1);
        }

        return false;
    }

    /**
     * Periodically evicts expired entries from the maps.
     * Entries are considered expired if they are older than the TTL period.
     */
    private void evictExpiredEntries() {
        final var currentTime = System.currentTimeMillis();
        final var iterator = timestampMap.entrySet().iterator();

        while (iterator.hasNext()) {
            final var entry = iterator.next();
            final var key = entry.getKey();
            final var timestamp = entry.getValue();

            if (currentTime - timestamp > TTL_PERIOD_MS) {
                // Remove expired entries from both maps.
                requestCountMap.remove(key);
                iterator.remove();
            }
        }
    }

    /**
     * Shuts down the eviction task when the service is destroyed.
     */
    @PreDestroy
    public void stopEvictionTask() {
        evictionExecutor.shutdownNow();
    }

}
