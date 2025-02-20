package com.akgarg.us.apigw.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@Order(-2)
public class JsonErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    /**
     * Create a new {@code AbstractErrorWebExceptionHandler}.
     *
     * @param errorAttributes       the error attributes
     * @param webProperties         the web configuration properties
     * @param applicationContext    the application context
     * @param serverCodecConfigurer codec configurer for HTTP message
     * @since 2.4.0
     */
    public JsonErrorWebExceptionHandler(
            final ErrorAttributes errorAttributes,
            final WebProperties webProperties,
            final ApplicationContext applicationContext,
            final ServerCodecConfigurer serverCodecConfigurer
    ) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::errorJsonResponse);
    }

    @Override
    protected void logError(final ServerRequest request, final ServerResponse response, final Throwable throwable) {
        log.error(
                "'{}' in processing request: {}",
                throwable.getClass().getSimpleName(),
                throwable.getMessage()
        );
        if (log.isDebugEnabled()) {
            log.error("Exception stacktrace:", throwable);
        }
    }

    private Mono<ServerResponse> errorJsonResponse(final ServerRequest request) {
        final var errorAttributes = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        final var throwable = getError(request);
        final ApiErrorResponse errorResponse;

        if (throwable instanceof UsernameNotFoundException) {
            errorResponse = new ApiErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    errorAttributes.get("requestId"),
                    throwable.getMessage()
            );
        } else {
            errorResponse = getApiErrorResponse(errorAttributes);
        }

        return ServerResponse.status(errorResponse.statusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private ApiErrorResponse getApiErrorResponse(final Map<String, Object> errorAttributes) {
        final var errorHttpStatusCode = (int) errorAttributes.getOrDefault("status", 500);
        final var errorMessage = errorAttributes.getOrDefault("message", "Internal Server Error").toString();
        final var traceId = errorAttributes.get("requestId");
        return new ApiErrorResponse(
                errorHttpStatusCode,
                traceId,
                errorMessage
        );
    }

}
