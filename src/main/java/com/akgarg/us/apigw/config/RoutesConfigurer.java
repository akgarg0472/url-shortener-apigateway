package com.akgarg.us.apigw.config;

import com.akgarg.us.apigw.filter.AuthTokenFilter;
import com.akgarg.us.apigw.filter.RateLimiterFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Configuration
@RequiredArgsConstructor
public class RoutesConfigurer {

    private final RateLimiterFilter rateLimiterFilter;
    private final AuthTokenFilter authTokenFilter;

    @Bean
    public RouteLocator routeLocator(final RouteLocatorBuilder routeLocatorBuilder) {
        final var router = routeLocatorBuilder.routes();

        router.route("favicon.ico", r -> r
                .path("/favicon.ico")
                .filters(filterSpec -> filterSpec.setStatus(HttpStatus.NO_CONTENT))
                .uri("no://noop")
        );

        router.route("auth_service", r -> r
                .path(ApiRoutes.AUTH_API_PATH)
                .filters(filterSpec -> filterSpec.filters(rateLimiterFilter))
                .uri("lb://urlshortener-auth-service")
        );

        router.route("urlshortener_service", r -> r
                .path(ApiRoutes.URL_SHORTENER_API_PATH)
                .filters(filterSpec -> filterSpec.filters(rateLimiterFilter, authTokenFilter))
                .uri("lb://urlshortener-service")
        );

        router.route("urlshortener-statistics-service", r -> r
                .path(ApiRoutes.STATISTICS_API_PATH)
                .filters(filterSpec -> filterSpec.filters(rateLimiterFilter, authTokenFilter))
                .uri("lb://urlshortener-statistics-service")
        );

        router.route("urlshortener-profile-service", r -> r
                .path(ApiRoutes.PROFILE_API_PATH)
                .filters(filterSpec -> filterSpec.filters(rateLimiterFilter, authTokenFilter))
                .uri("lb://urlshortener-profile-service"));

        router.route("urlshortener-payment-service-paypal-webhook", r -> r
                .path(ApiRoutes.PAYPAL_WEBHOOK_API_PATH)
                .filters(filterSpec -> filterSpec.filters(rateLimiterFilter))
                .uri("lb://urlshortener-payment-service"));

        router.route("urlshortener-payment-service", r -> r
                .path(ApiRoutes.PAYMENT_API_PATH)
                .filters(filterSpec -> filterSpec.filters(rateLimiterFilter, authTokenFilter))
                .uri("lb://urlshortener-payment-service"));

        router.route("urlshortener-subscription-packs", r -> r
                .path(ApiRoutes.SUBSCRIPTION_PACKS_API_PATH)
                .and()
                .method(HttpMethod.GET)
                .filters(filterSpec -> filterSpec.filters(rateLimiterFilter))
                .uri("lb://urlshortener-subscription-service"));

        router.route("urlshortener-subscription-service", r -> r
                .path(ApiRoutes.SUBSCRIPTION_API_PATH)
                .filters(filterSpec -> filterSpec.filters(rateLimiterFilter, authTokenFilter))
                .uri("lb://urlshortener-subscription-service"));

        router.route("urlshortener_service_public", r -> r
                .path("/**")
                .and()
                .not(r1 -> r1.path("/actuator/**"))
                .filters(filterSpec -> filterSpec.filters(rateLimiterFilter))
                .uri("lb://urlshortener-service")
        );

        return router.build();
    }

}
