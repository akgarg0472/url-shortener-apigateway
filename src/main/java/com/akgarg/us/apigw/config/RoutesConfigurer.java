package com.akgarg.us.apigw.config;

import com.akgarg.us.apigw.filter.AuthTokenFilter;
import com.akgarg.us.apigw.filter.RateLimiterFilter;
import com.akgarg.us.apigw.filter.RequestIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class RoutesConfigurer {

    private final RateLimiterFilter rateLimiterFilter;
    private final AuthTokenFilter authTokenFilter;
    private final RequestIdFilter requestIdFilter;

    @Bean
    public RouteLocator routeLocator(final RouteLocatorBuilder routeLocatorBuilder) {
        final var routes = routeLocatorBuilder.routes();

        routes.route("auth_service", r -> r
                .path(ApiRoutes.AUTH_API_PATH)
                .filters(filterSpec -> filterSpec
                        .filters(requestIdFilter, rateLimiterFilter)
                        .rewritePath("/api/(?<version>.*)/auth/(?<segment>.*)", "/auth/${version}/${segment}"))
                .uri("lb://urlshortener-auth-service")
        );

        routes.route("urlshortener_service", r -> r
                .path(ApiRoutes.URL_SHORTENER_API_PATH)
                .filters(filterSpec -> filterSpec.filters(requestIdFilter, rateLimiterFilter, authTokenFilter))
                .uri("lb://urlshortener-service")
        );

        routes.route("urlshortener-statistics-service", r -> r
                .path(ApiRoutes.STATISTICS_API_PATH)
                .filters(filterSpec -> filterSpec.filters(requestIdFilter, rateLimiterFilter, authTokenFilter))
                .uri("lb://urlshortener-statistics-service")
        );

        routes.route("urlshortener-profile-service", r -> r
                .path(ApiRoutes.PROFILE_API_PATH)
                .filters(filterSpec -> filterSpec.filters(requestIdFilter, rateLimiterFilter, authTokenFilter))
                .uri("lb://urlshortener-profile-service"));

        routes.route("urlshortener-payment-service", r -> r
                .path(ApiRoutes.PAYMENT_API_PATH)
//                .filters(filterSpec -> filterSpec.filters(requestIdFilter, rateLimiterFilter, authTokenFilter))
                .uri("lb://urlshortener-payment-service"));

        routes.route("urlshortener_service_public", r -> r
                .path("/**")
                .filters(filterSpec -> filterSpec.filters(requestIdFilter, rateLimiterFilter))
                .uri("lb://urlshortener-service")
        );

        return routes.build();
    }

}
