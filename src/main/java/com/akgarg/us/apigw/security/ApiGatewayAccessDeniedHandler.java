package com.akgarg.us.apigw.security;

import com.akgarg.us.apigw.error.ApiErrorResponse;
import com.akgarg.us.apigw.exception.ApiGatewayException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static com.akgarg.us.apigw.filter.RequestIdFilter.REQUEST_ID_HEADER_NAME;

@Component
@AllArgsConstructor
public class ApiGatewayAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException deniedException) {
        final var errorResponse = ApiErrorResponse.forbidden(extractRequestId(exchange), "You're not authorized to access the requested resource");
        final String responseBody;

        try {
            responseBody = objectMapper.writeValueAsString(errorResponse);
        } catch (Exception e) {
            throw new ApiGatewayException("Error while serializing error response", e);
        }

        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse().writeWith(Mono.fromSupplier(() -> {
            final var bufferFactory = exchange.getResponse().bufferFactory();
            return bufferFactory.wrap(responseBody.getBytes(StandardCharsets.UTF_8));
        }));
    }

    private String extractRequestId(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER_NAME);
    }

}