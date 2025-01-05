package com.akgarg.us.apigw.ratelimiter;

import com.akgarg.us.apigw.config.ApiRoutes;

import java.util.Map;

/**
 * {@code AbstractRateLimiterService} provides the base functionality for rate-limiting services.
 * It implements the {@link RateLimiterService} interface and defines common methods and configurations
 * shared across different rate limiter implementations.
 * <p>
 * This abstract class holds a static map to configure the allowed requests per minute for various API paths.
 * It also provides a utility method for generating a unique key based on the API path and the identifier (e.g., user ID or IP address).
 * Subclasses should implement the specific rate-limiting logic.
 * <p>
 */
public abstract class AbstractRateLimiterService implements RateLimiterService {

    /**
     * A map storing the allowed number of requests per minute for each API path.
     * The key is the API path, and the value is the allowed number of requests per minute.
     * <p>
     * The following paths and their limits are configured:
     * <ul>
     * <li>{@link ApiRoutes#AUTH_API_PATH}: 10 requests per minute</li>
     * <li>{@link ApiRoutes#URL_SHORTENER_API_PATH}: 10 requests per minute</li>
     * <li>{@link ApiRoutes#STATISTICS_API_PATH}: 50 requests per minute</li>
     * <li>{@link ApiRoutes#PROFILE_API_PATH}: 10 requests per minute</li>
     * <li>{@link ApiRoutes#PAYMENT_API_PATH}: 10 requests per minute</li>
     * <li>{@link ApiRoutes#GENERIC_API_PATH}: 50 requests per minute</li>
     * </ul>
     * </p>
     */
    static final Map<String, Integer> allowedRequestsPerMinute;

    static {
        allowedRequestsPerMinute = Map.of(
                ApiRoutes.AUTH_API_PATH, 10,
                ApiRoutes.URL_SHORTENER_API_PATH, 10,
                ApiRoutes.STATISTICS_API_PATH, 50,
                ApiRoutes.PROFILE_API_PATH, 10,
                ApiRoutes.PAYMENT_API_PATH, 10,
                ApiRoutes.GENERIC_API_PATH, 50
        );
    }

    /**
     * Creates a unique key for each request based on the given API path and identifier.
     * The key is a combination of the {@code path} and {@code identifier}, separated by a colon.
     * <p>
     * This key is used to track the request count and timestamp for a specific client
     * (e.g., identified by IP address or user ID) for a particular API path.
     * </p>
     *
     * @param path       The API path being requested (e.g., "/api/v1/statistics/**").
     * @param identifier The identifier of the client making the request (e.g., IP address or user ID).
     * @return A unique key representing the combination of the path and identifier.
     */
    public String createKey(final String path, final String identifier) {
        return String.format("%s:%s", path, identifier);
    }

}
