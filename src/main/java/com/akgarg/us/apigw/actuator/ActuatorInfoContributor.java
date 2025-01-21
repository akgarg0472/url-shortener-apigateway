package com.akgarg.us.apigw.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("squid:S1192")
public class ActuatorInfoContributor implements InfoContributor {

    private final Environment environment;

    @Override
    public void contribute(final Info.Builder builder) {
        try (final var inputStream = getClass().getClassLoader().getResourceAsStream("META-INF/build-info.properties")) {
            final var properties = new Properties();
            properties.load(inputStream);

            final var appInfo = Map.of(
                    "artifact", properties.getProperty("build.artifact", "ApiGateway"),
                    "version", properties.getProperty("build.version", null),
                    "name", properties.getProperty("build.name", null),
                    "description", properties.getProperty("build.description", null)
            );

            final var buildInfo = Map.of(
                    "java.version", properties.getProperty("build.java.version", null),
                    "buildTime", properties.getProperty("build.time"),
                    "spring.boot.version", properties.getProperty("build.spring.boot.version", null)
            );

            final var runtimeInfo = Map.of(
                    "port", environment.getProperty("local.server.port", "null"),
                    "profile", environment.getActiveProfiles(),
                    "java", Map.of(
                            "version", environment.getProperty("java.version", "null"),
                            "vendor", environment.getProperty("java.vendor", "null"),
                            "vendor.url", environment.getProperty("java.vendor.url", "null"),
                            "home", environment.getProperty("java.home", "null"),
                            "arch", environment.getProperty("os.arch", "null")
                    )
            );

            builder.withDetail("app", appInfo);
            builder.withDetail("build", buildInfo);
            builder.withDetail("runtime", runtimeInfo);

        } catch (Exception e) {
            log.error("Error reading build info", e);
        }
    }

}
