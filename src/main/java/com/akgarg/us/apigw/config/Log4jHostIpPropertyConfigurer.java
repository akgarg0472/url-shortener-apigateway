package com.akgarg.us.apigw.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
@RequiredArgsConstructor
public class Log4jHostIpPropertyConfigurer {

    @EventListener
    public void onApplicationEvent(final WebServerInitializedEvent event) {
        try {
            final var port = event.getWebServer().getPort();
            System.setProperty("LOGGING_PORT", String.valueOf(port));
            final var ip = InetAddress.getLocalHost().getHostAddress();
            System.setProperty("LOGGING_HOST", ip);
        } catch (UnknownHostException e) {
            log.error("Error getting local host address", e);
        }
    }

}