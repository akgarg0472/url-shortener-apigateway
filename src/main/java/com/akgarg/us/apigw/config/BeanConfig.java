package com.akgarg.us.apigw.config;

import com.akgarg.client.authclient.AuthClient;
import com.akgarg.client.authclient.AuthClientBuilder;
import com.akgarg.client.authclient.cache.AuthTokenCacheStrategy;
import com.akgarg.client.authclient.common.ApiVersion;
import com.akgarg.client.authclient.config.RedisConnectionConfigs;
import com.akgarg.client.authclient.config.RedisConnectionPoolConfigs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
public class BeanConfig {

    @Profile("dev")
    @Bean
    public AuthClient inMemoryAuthClient() {
        return AuthClientBuilder.builder()
                .apiVersion(ApiVersion.V1)
                .cacheStrategy(AuthTokenCacheStrategy.IN_MEMORY)
                .build();
    }

    @Profile("prod")
    @Bean
    public AuthClient redisAuthClient(final Environment environment) {
        final var redisConnectionConfigs = new RedisConnectionConfigs(
                environment.getProperty("auth.client.redis.host", "localhost"),
                Integer.parseInt(environment.getProperty("auth.client.redis.port", "6379"))
        );
        final var redisConnectionPoolConfigs = new RedisConnectionPoolConfigs(
                Integer.parseInt(environment.getProperty("auth.client.redis.pool.max-total", "128")),
                Integer.parseInt(environment.getProperty("auth.client.redis.pool.max-idle", "128")),
                Integer.parseInt(environment.getProperty("auth.client.redis.pool.min-idle", "16"))
        );
        return AuthClientBuilder.builder()
                .cacheStrategy(AuthTokenCacheStrategy.REDIS)
                .redisConnectionProperties(redisConnectionConfigs)
                .redisConnectionPoolConfig(redisConnectionPoolConfigs)
                .apiVersion(ApiVersion.V1)
                .build();
    }

}
