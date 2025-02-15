package com.akgarg.us.apigw.utils;

import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

public final class IpUtils {

    private IpUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static Optional<String> extractClientIp(final ServerWebExchange exchange) {
        final var ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");

        if (ip != null && !ip.isEmpty()) {
            return Optional.of(ip.split(",")[0].trim());
        }

        final var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? Optional.ofNullable(remoteAddress.getAddress().getHostAddress()) : Optional.empty();
    }

}
