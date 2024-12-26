package com.akgarg.us.apigw.config;

import com.akgarg.client.authclient.AuthClient;
import com.akgarg.client.authclient.AuthClientBuilder;
import com.akgarg.client.authclient.cache.AuthTokenCacheStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class BeanConfig {

    @Profile("dev")
    @Bean
    public AuthClient inMemoryAuthClient() {
        return AuthClientBuilder.builder()
                .cacheStrategy(AuthTokenCacheStrategy.IN_MEMORY)
                .build();
    }

    @Profile("prod")
    @Bean
    public AuthClient redisAuthClient() {
        return AuthClientBuilder.builder()
                .cacheStrategy(AuthTokenCacheStrategy.REDIS)
                .build();
    }

}
