package com.akgarg.us.apigw.urlshortnerapigateway.config;

import com.akgarg.us.apigw.urlshortnerapigateway.filter.AuthFilter;
import com.akgarg.us.apigw.urlshortnerapigateway.filter.ErrorHandlingFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutesConfigurer {

    private final AuthFilter authFilter;

    public RoutesConfigurer(final AuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator routeLocator(final RouteLocatorBuilder routeLocatorBuilder) {
        final var routes = routeLocatorBuilder.routes();

        routes.route("auth_service", r -> r
                .path("/api/v1/auth/**")
                .filters(filterSpec -> filterSpec
                        .rewritePath("/api/v1/auth/(?<segment>.*)", "/auth/${segment}"))
                .uri("lb://urlshortener-auth-service"));

        routes.route("urlshortener_service", r -> r
                .path("/api/v1/urlshortener/**")
                .filters(filterSpec -> filterSpec
                        .filters(authFilter)
                        .rewritePath("/api/v1/urlshortener/(?<segment>.*)", "/urlshortener/${segment}"))
                .uri("lb://urlshortener-service"));

        routes.route("urlshortener_service_public", r -> r
                .path("/**")
                .uri("lb://urlshortener-service"));

        return routes.build();
    }

    @Bean
    public GlobalFilter errorGlobalFilter(final ErrorHandlingFilter errorHandlingFilter) {
        return errorHandlingFilter;
    }

}
