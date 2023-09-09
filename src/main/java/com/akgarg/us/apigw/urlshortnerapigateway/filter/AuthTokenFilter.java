package com.akgarg.us.apigw.urlshortnerapigateway.filter;

import com.akgarg.client.authclient.AuthClientBuilder;
import com.akgarg.client.authclient.cache.AuthTokenCacheStrategy;
import com.akgarg.client.authclient.client.AuthClient;
import com.akgarg.client.authclient.common.AuthServiceEndpoint;
import com.akgarg.client.authclient.common.ValidateTokenRequest;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AuthTokenFilter implements GatewayFilter {

    private static final String USER_ID_HEADER_NAME = "X-USER-ID";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String UNAUTHENTICATED_RESPONSE = """
            {
                "message": "Unauthorized",
                "description": "Please log in to access resource",
                "code": 401
            }""";

    private final DiscoveryClient discoveryClient;
    private final AuthClient authClient;

    public AuthTokenFilter(final DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        this.authClient = AuthClientBuilder.builder().cacheStrategy(AuthTokenCacheStrategy.IN_MEMORY).build();
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        final var tokenValidated = validateToken(exchange.getRequest().getHeaders());

        if (!tokenValidated) {
            final var httpResponse = exchange.getResponse();
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
            return false;
        }

        final var validateTokenRequest = new ValidateTokenRequest(userId.get(), authToken.get(), getAuthServiceEndpoints());

        return authClient.validate(validateTokenRequest);
    }

    private Optional<String> extractAuthTokenFromRequestHeader(final HttpHeaders headers) {
        final var headerValues = headers.get(AUTHORIZATION_HEADER_NAME);

        if (headerValues == null || headerValues.size() != 1) {
            return Optional.empty();
        }

        final var authHeaderValue = headerValues.get(0);

        if (authHeaderValue == null || authHeaderValue.isBlank() || !authHeaderValue.startsWith("Bearer ")) {
            return Optional.empty();
        }

        return Optional.of(authHeaderValue.substring(7));
    }

    private Optional<String> extractUserIdFromRequestHeader(final HttpHeaders headers) {
        final var headerValues = headers.get(USER_ID_HEADER_NAME);

        if (headerValues == null || headerValues.size() != 1) {
            return Optional.empty();
        }

        return Optional.of(headerValues.get(0).trim());
    }

    private List<AuthServiceEndpoint> getAuthServiceEndpoints() {
        final List<AuthServiceEndpoint> endpoints = new ArrayList<>();
        final var authServiceInstances = discoveryClient.getInstances("urlshortener-auth-service");

        authServiceInstances.forEach(instance -> {
            final var endpoint = new AuthServiceEndpoint(instance.getScheme(), instance.getHost(), instance.getPort());
            endpoints.add(endpoint);
        });

        return endpoints;
    }

}
