package com.akgarg.us.apigw.filter;

import com.akgarg.us.apigw.config.ApiRoutes;
import com.akgarg.us.apigw.ratelimiter.RateLimiter;
import com.akgarg.us.apigw.ratelimiter.RateLimitingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends AbstractApiGatewayFilter {

    private static final Map<String, RateLimitingStrategy> rateLimiterPathStrategies = new LinkedHashMap<>();
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final String IP_FETCH_FAILURE_RESPONSE = """
            {
                "message": "Request validation Failed",
                "description": "Unable to retrieve client IP address.",
                "code": 400
            }""";
    private static final String USER_ID_FETCH_FAILURE_RESPONSE = """
            {
                "message": "Request validation Failed",
                "description": "Unable to retrieve userId.",
                "code": 400
            }""";
    private static final String RATE_LIMIT_EXCEEDED_RESPONSE = """
            {
                "message": "Rate Limit Exceeded",
                "description": "You have exceeded the number of allowed requests. Please try again later.",
                "code": 429
            }""";

    static {
        rateLimiterPathStrategies.put(ApiRoutes.SUBSCRIPTION_PACKS_API_PATH, RateLimitingStrategy.IP);
        rateLimiterPathStrategies.put(ApiRoutes.URL_SHORTENER_API_PATH, RateLimitingStrategy.USER_ID);
        rateLimiterPathStrategies.put(ApiRoutes.SUBSCRIPTION_API_PATH, RateLimitingStrategy.USER_ID);
        rateLimiterPathStrategies.put(ApiRoutes.STATISTICS_API_PATH, RateLimitingStrategy.USER_ID);
        rateLimiterPathStrategies.put(ApiRoutes.PROFILE_API_PATH, RateLimitingStrategy.USER_ID);
        rateLimiterPathStrategies.put(ApiRoutes.PAYMENT_API_PATH, RateLimitingStrategy.USER_ID);
        rateLimiterPathStrategies.put(ApiRoutes.AUTH_API_PATH, RateLimitingStrategy.IP);
        rateLimiterPathStrategies.put(ApiRoutes.GENERIC_API_PATH, RateLimitingStrategy.IP);
    }

    private final RateLimiter rateLimiter;

    @Override
    @SuppressWarnings("squid:S135")
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        final var requestPath = exchange.getRequest().getURI().getPath();

        log.debug("Checking rate limit for path: {}", requestPath);

        for (final var pathStrategy : rateLimiterPathStrategies.entrySet()) {
            if (!pathMatcher.match(pathStrategy.getKey(), requestPath)) {
                continue;
            }

            final boolean isRateLimited;

            if (pathStrategy.getValue() == RateLimitingStrategy.USER_ID) {
                final var userId = extractUserIdFromRequestHeader(exchange.getRequest().getHeaders());

                if (userId.isEmpty()) {
                    final var httpResponse = exchange.getResponse();
                    httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
                    httpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    return httpResponse.writeWith(Mono.just(httpResponse.bufferFactory().wrap(USER_ID_FETCH_FAILURE_RESPONSE.getBytes())));
                }

                isRateLimited = rateLimiter.isRateLimited(pathStrategy.getKey(), requestPath, userId.get());
            } else {
                final var ip = extractClientIp(exchange);

                if (ip.isEmpty()) {
                    final var httpResponse = exchange.getResponse();
                    httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
                    httpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    return httpResponse.writeWith(Mono.just(httpResponse.bufferFactory().wrap(IP_FETCH_FAILURE_RESPONSE.getBytes())));
                }

                isRateLimited = rateLimiter.isRateLimited(pathStrategy.getKey(), requestPath, ip.get());
            }

            if (isRateLimited) {
                final var httpResponse = exchange.getResponse();
                httpResponse.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                httpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                return httpResponse.writeWith(Mono.just(httpResponse.bufferFactory().wrap(RATE_LIMIT_EXCEEDED_RESPONSE.getBytes())));
            }

            break;
        }

        return chain.filter(exchange);
    }

    private Optional<String> extractClientIp(final ServerWebExchange exchange) {
        final var ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");

        if (ip != null && !ip.isEmpty()) {
            final var ips = ip.split(",");
            return Optional.of(ips[0].trim());
        }

        final var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? Optional.ofNullable(remoteAddress.getAddress().getHostAddress()) : Optional.empty();
    }

}
