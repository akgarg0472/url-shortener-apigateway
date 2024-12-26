package com.akgarg.us.apigw.error;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(final ServerRequest request, final ErrorAttributeOptions options) {
        final Map<String, Object> errorAttributes = HashMap.newHashMap(3);
        final var error = getError(request);
        final var responseStatusAnnotation = MergedAnnotations
                .from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(ResponseStatus.class);
        final HttpStatus errorHttpStatus = getErrorHttpStatus(error, responseStatusAnnotation);

        errorAttributes.put("status", errorHttpStatus.value());
        errorAttributes.put("message", errorHttpStatus.getReasonPhrase());
        errorAttributes.put("traceId", request.exchange().getLogPrefix());

        return errorAttributes;
    }

    private HttpStatus getErrorHttpStatus(
            final Throwable error,
            final MergedAnnotation<ResponseStatus> responseStatusAnnotation
    ) {
        if (error instanceof ResponseStatusException responseStatusException) {
            return HttpStatus.valueOf(responseStatusException.getStatusCode().value());
        }

        if (error instanceof ConnectException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        if (responseStatusAnnotation.isPresent()) {
            return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
