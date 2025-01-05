package com.akgarg.us.apigw.ratelimiter;

/**
 * The {@code RateLimiterService} interface defines the contract for rate-limiting functionality.
 * Implementations of this interface should provide logic to determine if a request is rate-limited
 * based on a specific API path and client identifier (e.g., IP address or user ID).
 * <p>
 * Rate limiting is useful for controlling the frequency of requests to specific endpoints,
 * ensuring fair usage, and protecting against abuse such as brute-force attacks.
 */
public interface RateLimiterService {

    /**
     * Checks if a request is rate-limited based on the provided path and client identifier.
     *
     * <p>This method should determine if the number of requests for the given API path and identifier
     * has exceeded the predefined rate limit. The identifier could represent a user's IP address,
     * user ID, or any other unique client identifier.</p>
     *
     * @param requestedPath the API path or endpoint that the client is trying to access.
     *                      This could be a specific URL or route pattern.
     * @param identifier    the client identifier for the request, such as the user's IP address
     *                      or user ID. This value is used to track the client's request count.
     * @return {@code true} if the request is rate-limited (i.e., the request count exceeds the allowed
     * rate for the specified path and identifier), otherwise {@code false}.
     * @throws IllegalArgumentException if either of the parameters is {@code null} or empty.
     */
    boolean isRateLimited(String requestedPath, String identifier);

}
