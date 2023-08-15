package com.akgarg.us.apigw.urlshortnerapigateway.filter;

import com.akgarg.us.apigw.urlshortnerapigateway.service.AuthService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GatewayFilter {

    private final AuthService authService;

    public AuthFilter(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        System.out.println("Auth filter called for " + exchange.getRequest().getPath());
        System.out.println(authService.isAuthorized());
        return chain.filter(exchange);
    }

}
