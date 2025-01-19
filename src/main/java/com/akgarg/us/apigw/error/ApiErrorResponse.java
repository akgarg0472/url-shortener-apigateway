package com.akgarg.us.apigw.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

public record ApiErrorResponse(
        @JsonProperty("status_code") int statusCode,
        @JsonProperty("request_id") Object requestId,
        @JsonProperty("error_message") String message) {

    public static ApiErrorResponse forbidden(final String requestId, final String message) {
        return new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), requestId, message);
    }

}