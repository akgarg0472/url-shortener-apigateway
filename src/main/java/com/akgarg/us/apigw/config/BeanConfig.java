package com.akgarg.us.apigw.config;

import com.akgarg.client.authclient.AuthClient;
import com.akgarg.client.authclient.AuthClientBuilder;
import com.akgarg.client.authclient.cache.AuthTokenCacheStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public AuthClient authClient() {
        return AuthClientBuilder.builder()
                .cacheStrategy(AuthTokenCacheStrategy.IN_MEMORY)
                .build();
    }

}
