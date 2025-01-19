package com.akgarg.us.apigw.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@Profile("prod")
@AllArgsConstructor
public class AuthServiceUserDetailsService implements ReactiveUserDetailsService {

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    private final WebClient.Builder authServiceWebClientBuilder;
    private final Environment environment;

    @Override
    public Mono<UserDetails> findByUsername(final String username) throws UsernameNotFoundException {
        final var verifyAdminEndpoint = environment.getProperty(
                "auth.service.endpoints.verify-admin",
                "/api/v1/auth/verify-admin"
        );

        return authServiceWebClientBuilder.build()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path(verifyAdminEndpoint)
                        .build())
                .header(REQUEST_ID_HEADER_NAME, UUID.randomUUID().toString())
                .bodyValue(Map.of("user_id", username))
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Error response received from auth service: {}", body);
                                    return Mono.error(new ResponseStatusException(clientResponse.statusCode(), body));
                                });
                    }

                    return clientResponse.bodyToMono(Map.class)
                            .flatMap(response -> {
                                if (response == null || response.isEmpty()) {
                                    return Mono.error(new IllegalStateException("Invalid response received from auth service"));
                                }

                                final var success = Boolean.parseBoolean(String.valueOf(response.get("success")));

                                if (success) {
                                    return Mono.just((UserDetails) new User(username, "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
                                } else {
                                    return Mono.error(new UsernameNotFoundException("No user found with id: %s".formatted(username)));
                                }
                            });
                })
                .onErrorResume(e -> Mono.error(new UsernameNotFoundException("Failed to check admin user for userId: %s".formatted(username))));
    }

}
