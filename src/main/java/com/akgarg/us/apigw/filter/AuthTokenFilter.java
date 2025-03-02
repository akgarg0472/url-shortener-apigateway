package com.akgarg.us.apigw.filter;

import com.akgarg.client.authclient.AuthClient;
import com.akgarg.client.authclient.common.AuthServiceEndpoint;
import com.akgarg.client.authclient.common.ValidateTokenRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends AbstractApiGatewayFilter {

    private static final String UNAUTHENTICATED_RESPONSE = """
            {
                "message": "Unauthorized",
                "description": "Please log in to access requested resource",
                "code": 401
            }""";
    private static final String AUTH_SERVICE_NAME = "urlshortener-auth-service";
    private static final String AUTH_COOKIE_NAME = "auth_token";

    private final DiscoveryClient discoveryClient;
    private final AuthClient authClient;

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        final var tokenValidated = validateToken(exchange.getRequest());

        if (!tokenValidated) {
            log.info("Token validation failed for request: {}", exchange.getRequest().getPath());
            final var httpResponse = exchange.getResponse();
            httpResponse.setStatusCode(HttpStatusCode.valueOf(401));
            httpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            return httpResponse.writeWith(Mono.just(httpResponse.bufferFactory().wrap(UNAUTHENTICATED_RESPONSE.getBytes())));
        }

        return chain.filter(exchange);
    }

    private boolean validateToken(final ServerHttpRequest httpRequest) {
        final var userId = extractUserIdFromRequestHeader(httpRequest.getHeaders());
        final var authToken = extractAuthTokenFromRequest(httpRequest);

        if (userId.isEmpty() || authToken.isEmpty()) {
            log.info("Token validation failed because user id or auth token is empty");
            return false;
        }

        final var validateTokenRequest = new ValidateTokenRequest(userId.get(), authToken.get(), getAuthServiceEndpoints());

        return authClient.validate(validateTokenRequest);
    }

    private Optional<String> extractAuthTokenFromRequest(final ServerHttpRequest httpRequest) {
        final var cookies = httpRequest.getCookies().get(AUTH_COOKIE_NAME);

        if (cookies == null || cookies.size() != 1) {
            log.debug("Auth token cookie is unavailable or has multiple values");
            return Optional.empty();
        }

        final var authCookie = cookies.getFirst().getValue();

        if (authCookie.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(authCookie);
    }

    private List<AuthServiceEndpoint> getAuthServiceEndpoints() {
        final var endpoints = new ArrayList<AuthServiceEndpoint>();
        final var authServiceInstances = discoveryClient.getInstances(AUTH_SERVICE_NAME);

        authServiceInstances.forEach(instance -> {
            final var instanceUri = instance.getUri();
            final var endpoint = new AuthServiceEndpoint(instanceUri.getScheme(), instanceUri.getHost(), instanceUri.getPort());
            endpoints.add(endpoint);
        });

        return endpoints;
    }

}
