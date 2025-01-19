package com.akgarg.us.apigw.security;

import com.akgarg.us.apigw.error.response.ApiErrorResponse;
import com.akgarg.us.apigw.exception.ApiGatewayException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiGatewayAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(final ServerWebExchange exchange, final AuthenticationException ex) {
        final var errorResponse = new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                extractRequestId(exchange),
                "Authentication is required to access requested resource");

        final String responseBody;

        try {
            responseBody = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            throw new ApiGatewayException("Error stringify unauthorized response JSON", e);
        }

        return exchange.getResponse().writeWith(Mono.fromSupplier(() -> {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.bufferFactory().wrap(responseBody.getBytes());
        }));
    }

    private String extractRequestId(final ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("X-Request-ID");
    }

}