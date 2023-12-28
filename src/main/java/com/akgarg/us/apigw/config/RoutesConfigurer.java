package com.akgarg.us.apigw.config;

import com.akgarg.us.apigw.filter.AuthTokenFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutesConfigurer {

    private final AuthTokenFilter authTokenFilter;

    public RoutesConfigurer(final AuthTokenFilter authTokenFilter) {
        this.authTokenFilter = authTokenFilter;
    }

    @Bean
    public RouteLocator routeLocator(final RouteLocatorBuilder routeLocatorBuilder) {
        final var routes = routeLocatorBuilder.routes();

        routes.route("auth_service", r -> r
                .path("/api/v1/auth/**")
                .filters(filterSpec -> filterSpec
                        .rewritePath("/api/(?<version>.*)/auth/(?<segment>.*)", "/auth/${version}/${segment}"))
                .uri("lb://urlshortener-auth-service")
        );

        routes.route("urlshortener_service", r -> r
                .path("/api/v1/urlshortener/**")
                // .filters(filterSpec -> filterSpec
                // .filters(authTokenFilter))
                .uri("lb://urlshortener-service")
        );

        routes.route("urlshortener-statistics-service", r -> r
                .path("/api/v1/statistics/**")
                // .filters(filterSpec -> filterSpec.filters(authTokenFilter))
                .uri("lb://urlshortener-statistics-service")
        );

        routes.route("urlshortener_service_public", r -> r
                .path("/**")
                .uri("lb://urlshortener-service")
        );

        return routes.build();
    }

}
