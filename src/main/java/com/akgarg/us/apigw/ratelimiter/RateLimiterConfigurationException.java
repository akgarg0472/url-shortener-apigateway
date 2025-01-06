package com.akgarg.us.apigw.ratelimiter;

public class RateLimiterConfigurationException extends RuntimeException {

    public RateLimiterConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
