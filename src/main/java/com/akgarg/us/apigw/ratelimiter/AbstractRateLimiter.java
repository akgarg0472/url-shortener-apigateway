package com.akgarg.us.apigw.ratelimiter;

import com.akgarg.us.apigw.config.ApiRoutes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code AbstractRateLimiterService} provides the base functionality for rate-limiting services.
 * It implements the {@link RateLimiter} interface and defines common methods and configurations
 * shared across different rate limiter implementations.
 * <p>
 * This abstract class holds a static map to configure the allowed requests per minute for various API paths.
 * It also provides a utility method for generating a unique key based on the API path and the identifier (e.g., user ID or IP address).
 * Subclasses should implement the specific rate-limiting logic.
 * <p>
 */
@Slf4j
public abstract class AbstractRateLimiter implements RateLimiter {

    /**
     * A map storing the allowed number of requests for a certain interval for each API route.
     * The key is the API route, and the value is the allowed number of requests for the interval.
     * <p>
     * The following routes and their limits are configured:
     * <ul>
     * <li>{@link ApiRoutes#AUTH_API_PATH}: 10 requests for the interval</li>
     * <li>{@link ApiRoutes#URL_SHORTENER_API_PATH}: 10 requests for the interval</li>
     * <li>{@link ApiRoutes#STATISTICS_API_PATH}: 10 requests for the interval</li>
     * <li>{@link ApiRoutes#PROFILE_API_PATH}: 10 requests for the interval</li>
     * <li>{@link ApiRoutes#PAYMENT_API_PATH}: 10 requests for the interval</li>
     * <li>{@link ApiRoutes#GENERIC_API_PATH}: 50 requests for the interval</li>
     * <li>{@link ApiRoutes#PAYPAL_WEBHOOK_API_PATH}: 100 requests for the interval</li>
     * </ul>
     * </p>
     */
    static final Map<String, Integer> allowedRequests = new HashMap<>();

    static {
        allowedRequests.put(ApiRoutes.SUBSCRIPTION_PACKS_API_PATH, 50);
        allowedRequests.put(ApiRoutes.URL_SHORTENER_API_PATH, 10);
        allowedRequests.put(ApiRoutes.SUBSCRIPTION_API_PATH, 50);
        allowedRequests.put(ApiRoutes.STATISTICS_API_PATH, 10);
        allowedRequests.put(ApiRoutes.PROFILE_API_PATH, 10);
        allowedRequests.put(ApiRoutes.PAYMENT_API_PATH, 10);
        allowedRequests.put(ApiRoutes.GENERIC_API_PATH, 50);
        allowedRequests.put(ApiRoutes.AUTH_API_PATH, 10);
        allowedRequests.put(ApiRoutes.PAYPAL_WEBHOOK_API_PATH, 100);
    }

    /**
     * Creates a unique key for each request based on the given API path and identifier.
     * The key is a combination of the {@code path} and {@code identifier}, separated by a colon.
     * <p>
     * This key is used to track the request count and timestamp for a specific client
     * (e.g., identified by IP address or user ID) for a particular API path.
     * </p>
     *
     * @param path       The API path being requested excluding request params (e.g., "/api/v1/statistics/**").
     * @param identifier The identifier of the client making the request (e.g., IP address or user ID).
     * @return A unique key representing the combination of the path and identifier.
     */
    public String createKey(final String path, final String identifier) {
        return String.format("%s:%s", path, identifier);
    }

    /**
     * Updates the allowed number of requests per minute for each API path based on the environment properties.
     * This method retrieves rate-limiting configuration for various API paths from the environment and updates the
     * {@link #allowedRequests} map accordingly. The property for each API path should be in the format:
     * <code>rate-limiter.limits.per-minute.{path}</code>, where <code>{path}</code> is the API route, and the value
     * is the allowed number of requests per minute. If the property is found and valid, the rate limit is updated.
     *
     * @param environment The environment containing the rate-limiting configuration properties.
     * @throws RateLimiterConfigurationException if the property value cannot be parsed to an integer or is missing
     */
    void updateAllowedRequestsPerMinute(final Environment environment) {
        for (final var path : ApiRoutes.getApiPaths()) {
            final var property = environment.getProperty("rate-limiter.limits.per-minute." + path);
            log.info("Rate limit for {}: {}", path, property);

            if (property != null) {
                try {
                    allowedRequests.put(path, Integer.parseInt(property));
                } catch (Exception e) {
                    throw new RateLimiterConfigurationException("Failed to configure rate limit for " + path, e);
                }
            } else {
                allowedRequests.put(path, 1);
            }
        }
    }

}
