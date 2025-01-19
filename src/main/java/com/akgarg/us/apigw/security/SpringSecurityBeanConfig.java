package com.akgarg.us.apigw.security;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SpringSecurityBeanConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder authServiceWebClientBuilder() {
        return WebClient.builder()
                .baseUrl("http://urlshortener-auth-service");
    }
    
}
