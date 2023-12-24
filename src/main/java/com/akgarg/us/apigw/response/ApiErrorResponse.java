package com.akgarg.us.apigw.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiErrorResponse(
        @JsonProperty("status_code") int statusCode,
        @JsonProperty("trace_id") Object traceId,
        @JsonProperty("error_message") String message) {
}