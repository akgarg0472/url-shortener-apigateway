package com.akgarg.us.apigw.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Slf4j
@Component
@AllArgsConstructor
@SuppressWarnings("NullableProblems")
public class RequestHeaderAdminUserDetailsExtractionFilter implements WebFilter {

    private static final String AUTH_SET_CONTEXT_KEY = "authenticationSet";
    private static final String USER_ID_HEADER_NAME = "X-USER-ID";

    private final ReactiveUserDetailsService userDetailsService;

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain filterChain) {
        if (!AdminEndpointConfig.isAdminEndpoint(exchange.getRequest().getPath().value(), exchange.getRequest().getMethod())) {
            return filterChain.filter(exchange);
        }

        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(AUTH_SET_CONTEXT_KEY)) {
                return filterChain.filter(exchange);
            }

            final var username = exchange.getRequest().getHeaders().getFirst(USER_ID_HEADER_NAME);
            final var requestId = extractRequestId(exchange);

            log.info("{} checking if '{}' is admin user", requestId, username);

            if (username != null && !username.isBlank()) {
                return authenticateUser(username)
                        .doOnNext(authToken -> log.info("{} {} user is admin", requestId, username))
                        .flatMap(authToken -> filterChain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authToken))
                                .contextWrite(Context.of(AUTH_SET_CONTEXT_KEY, true)));
            }

            return filterChain.filter(exchange);
        });
    }

    private String extractRequestId(final ServerWebExchange exchange) {
        final var reqIdHeader = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
        if (reqIdHeader != null) {
            return reqIdHeader;
        }
        return exchange.getLogPrefix();
    }

    private Mono<UsernamePasswordAuthenticationToken> authenticateUser(final String username) {
        return userDetailsService.findByUsername(username)
                .map(userDetails -> new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()));
    }

}
