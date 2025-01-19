package com.akgarg.us.apigw.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@AllArgsConstructor
@EnableWebFluxSecurity
public class SpringSecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";

    private final RequestHeaderAdminUserDetailsExtractionFilter requestHeaderUserExtractionFilter;
    private final ApiGatewayAuthenticationEntryPoint authenticationEntryPoint;
    private final ApiGatewayAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityWebFilterChain securityFilterChain(final ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);
        http.formLogin(ServerHttpSecurity.FormLoginSpec::disable);
        http.addFilterAt(requestHeaderUserExtractionFilter, SecurityWebFiltersOrder.SECURITY_CONTEXT_SERVER_WEB_EXCHANGE);

        http.authorizeExchange(exchange -> {
                    for (final var endpoint : AdminEndpointConfig.getAdminOnlyEndpoints()) {
                        exchange.pathMatchers(endpoint.httpMethod(), endpoint.path()).hasRole(ROLE_ADMIN);
                    }
                    exchange.anyExchange().permitAll();
                }
        );

        http.exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
        );

        return http.build();
    }

}