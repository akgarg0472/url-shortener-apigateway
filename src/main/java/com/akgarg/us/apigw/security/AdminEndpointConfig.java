package com.akgarg.us.apigw.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
final class AdminEndpointConfig {

    private static final Collection<AdminEndpoint> ADMIN_ONLY_ENDPOINTS = new ArrayList<>();
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    static {
        final var subscriptionEndpoint = "/api/v1/subscriptions";
        final var subscriptionPackEndpoint = "/api/v1/subscriptions/packs/**";

        ADMIN_ONLY_ENDPOINTS.addAll(
                List.of(new AdminEndpoint(subscriptionEndpoint, HttpMethod.POST),
                        new AdminEndpoint(subscriptionPackEndpoint, HttpMethod.POST),
                        new AdminEndpoint(subscriptionPackEndpoint, HttpMethod.PATCH),
                        new AdminEndpoint(subscriptionPackEndpoint, HttpMethod.DELETE)
                )
        );
    }

    public static Collection<AdminEndpoint> getAdminOnlyEndpoints() {
        return ADMIN_ONLY_ENDPOINTS;
    }

    public static boolean isAdminEndpoint(final String path, final HttpMethod httpMethod) {
        return ADMIN_ONLY_ENDPOINTS.stream()
                .anyMatch(endpoint -> PATH_MATCHER.match(endpoint.path(), path) && endpoint.httpMethod().equals(httpMethod));
    }

    public record AdminEndpoint(String path, HttpMethod httpMethod) {
    }

}
