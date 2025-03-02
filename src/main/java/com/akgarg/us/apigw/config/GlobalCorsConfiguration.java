package com.akgarg.us.apigw.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
public class GlobalCorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter(final Environment environment) {
        final var corsConfig = new CorsConfiguration();
        final var frontendDomain = environment.getProperty("frontend.base.url", "http://localhost:3000");
        corsConfig.setAllowedOrigins(List.of(frontendDomain));
        corsConfig.setMaxAge(300L);
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfig.addAllowedHeader("*");
        corsConfig.setAllowCredentials(true);

        final var corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(corsConfigurationSource);
    }

}
