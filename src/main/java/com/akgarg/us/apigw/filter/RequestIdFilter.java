package com.akgarg.us.apigw.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestIdFilter implements GatewayFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        if (exchange.getRequest().getHeaders().containsKey(REQUEST_ID_HEADER)) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange.mutate()
                .request(builder -> builder.header(REQUEST_ID_HEADER, UUID.randomUUID().toString()))
                .build());
    }

}
