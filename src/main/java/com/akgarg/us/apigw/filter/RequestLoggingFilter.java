package com.akgarg.us.apigw.filter;

import com.akgarg.us.apigw.utils.IpUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private final MeterRegistry meterRegistry;

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        final var startTime = System.currentTimeMillis();

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    if (signalType == SignalType.ON_COMPLETE || signalType == SignalType.ON_ERROR) {
                        final var requestPath = exchange.getRequest().getURI().getPath();
                        final var method = exchange.getRequest().getMethod().name();
                        final var clientIp = IpUtils.extractClientIp(exchange).orElse("0.0.0.0");
                        final var statusCode = exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode().value() : HttpStatus.OK.value();
                        final var duration = System.currentTimeMillis() - startTime;

                        meterRegistry.counter(
                                "urlshortener_api_gateway_requests_total",
                                "method", method,
                                "path", requestPath,
                                "status", String.valueOf(statusCode),
                                "client_ip", clientIp
                        ).increment();

                        Timer.builder("urlshortener_api_gateway_request_duration")
                                .tags("method", method,
                                        "path", requestPath,
                                        "status", String.valueOf(statusCode),
                                        "client_ip", clientIp)
                                .publishPercentileHistogram()
                                .sla(Duration.ofMillis(100),
                                        Duration.ofMillis(300),
                                        Duration.ofMillis(500),
                                        Duration.ofSeconds(1),
                                        Duration.ofSeconds(3),
                                        Duration.ofSeconds(5),
                                        Duration.ofSeconds(10))
                                .register(meterRegistry)
                                .record(duration, TimeUnit.MILLISECONDS);

                        if (log.isInfoEnabled()) {
                            log.info("{\"method\": \"{}\", \"path\": \"{}\", \"client_ip\": \"{}\", \"status_code\": {}, \"response_time_ms\": {}}",
                                    method,
                                    requestPath,
                                    clientIp,
                                    statusCode,
                                    duration
                            );
                        }
                    }
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }

}
