package com.akgarg.us.apigw.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

@Slf4j
public abstract class AbstractApiGatewayFilter implements GatewayFilter {

    protected static final String USER_ID_HEADER_NAME = "X-USER-ID";

    Optional<String> extractUserIdFromRequestHeader(final HttpHeaders headers) {
        final var headerValues = headers.get(USER_ID_HEADER_NAME);

        if (headerValues == null || headerValues.size() != 1) {
            log.debug("User id header is unavailable or has multiple values");
            return Optional.empty();
        }

        return Optional.of(headerValues.getFirst().trim());
    }

}
