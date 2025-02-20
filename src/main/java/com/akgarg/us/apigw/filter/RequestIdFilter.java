package com.akgarg.us.apigw.filter;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class RequestIdFilter implements WebFilter {

    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String REQUEST_ID_ATTRIBUTE_NAME = "requestId";

    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        final var request = exchange.getRequest();
        final var response = exchange.getResponse();

        var requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER_NAME);

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        response.getHeaders().add(REQUEST_ID_HEADER_NAME, requestId);

        final var finalRequestId = requestId;

        return chain.filter(exchange.mutate()
                        .request(builder -> builder.header(REQUEST_ID_HEADER_NAME, finalRequestId).build())
                        .build())
                .doOnEach(signal -> ThreadContext.put(REQUEST_ID_ATTRIBUTE_NAME, finalRequestId))
                .contextWrite(ctx -> ctx.put(REQUEST_ID_ATTRIBUTE_NAME, finalRequestId))
                .doFinally(signalType -> ThreadContext.remove(REQUEST_ID_ATTRIBUTE_NAME));
    }

}
