package com.akgarg.us.apigw.filter;

import com.akgarg.client.authclient.AuthClient;
import com.akgarg.client.authclient.common.AuthServiceEndpoint;
import com.akgarg.client.authclient.common.ValidateTokenRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class AuthTokenFilter implements GatewayFilter {

    private static final String USER_ID_HEADER_NAME = "X-USER-ID";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String UNAUTHENTICATED_RESPONSE = """
            {
                "message": "Unauthorized",
                "description": "Please log in to access requested resource",
                "code": 401
            }""";
    private static final String AUTH_SERVICE_NAME = "urlshortener-auth-service";

    private final DiscoveryClient discoveryClient;
    private final AuthClient authClient;

    public AuthTokenFilter(
            final DiscoveryClient discoveryClient,
            final AuthClient authClient
    ) {
        this.discoveryClient = discoveryClient;
        this.authClient = authClient;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        final var tokenValidated = validateToken(exchange.getRequest().getHeaders());

        if (!tokenValidated) {
            log.trace("Token validation failed for request: {}", exchange.getRequest().getPath());
            final ServerHttpResponse httpResponse = exchange.getResponse();
            httpResponse.setStatusCode(HttpStatusCode.valueOf(401));
            httpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            return httpResponse.writeWith(Mono.just(httpResponse.bufferFactory().wrap(UNAUTHENTICATED_RESPONSE.getBytes())));
        }

        return chain.filter(exchange);
    }

    private boolean validateToken(final HttpHeaders headers) {
        final var userId = extractUserIdFromRequestHeader(headers);
        final var authToken = extractAuthTokenFromRequestHeader(headers);

        if (userId.isEmpty() || authToken.isEmpty()) {
            log.info("Token validation failed because user id or auth token is empty");
            return false;
        }

        final var validateTokenRequest = new ValidateTokenRequest(userId.get(), authToken.get(), getAuthServiceEndpoints());

        return authClient.validate(validateTokenRequest);
    }

    private Optional<String> extractAuthTokenFromRequestHeader(final HttpHeaders headers) {
        final List<String> headerValues = headers.get(AUTHORIZATION_HEADER_NAME);

        if (headerValues == null || headerValues.size() != 1) {
            log.debug("Auth header is unavailable or has multiple values");
            return Optional.empty();
        }

        final String authHeaderValue = headerValues.getFirst();

        if (authHeaderValue == null || authHeaderValue.isBlank() || !authHeaderValue.startsWith("Bearer ")) {
            return Optional.empty();
        }

        return Optional.of(authHeaderValue.substring(7));
    }

    private Optional<String> extractUserIdFromRequestHeader(final HttpHeaders headers) {
        final var headerValues = headers.get(USER_ID_HEADER_NAME);

        if (headerValues == null || headerValues.size() != 1) {
            log.debug("User id header is unavailable or has multiple values");
            return Optional.empty();
        }

        return Optional.of(headerValues.getFirst().trim());
    }

    private List<AuthServiceEndpoint> getAuthServiceEndpoints() {
        final var endpoints = new ArrayList<AuthServiceEndpoint>();
        final var authServiceInstances = discoveryClient.getInstances(AUTH_SERVICE_NAME);

        authServiceInstances.forEach(instance -> {
            final var endpoint = new AuthServiceEndpoint(instance.getScheme(), instance.getHost(), instance.getPort());
            endpoints.add(endpoint);
        });

        return endpoints;
    }

}
