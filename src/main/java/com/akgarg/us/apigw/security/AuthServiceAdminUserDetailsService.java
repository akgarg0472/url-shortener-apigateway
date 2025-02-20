package com.akgarg.us.apigw.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.akgarg.us.apigw.filter.RequestIdFilter.REQUEST_ID_HEADER_NAME;

@Slf4j
@Component
@Profile("prod")
@AllArgsConstructor
public class AuthServiceAdminUserDetailsService implements ReactiveUserDetailsService {

    private static final ParameterizedTypeReference<Map<String, Object>> parameterizedTypeReference = new ParameterizedTypeReference<>() {
    };

    private final WebClient.Builder authServiceWebClientBuilder;
    private final Environment environment;

    @Override
    public Mono<UserDetails> findByUsername(final String username) throws UsernameNotFoundException {
        final String verifyAdminEndpoint = environment.getProperty(
                "auth.service.endpoints.verify-admin",
                "/api/v1/auth/verify-admin"
        );

        return authServiceWebClientBuilder.build()
                .post()
                .uri(uriBuilder -> uriBuilder.path(verifyAdminEndpoint).build())
                .header(REQUEST_ID_HEADER_NAME, UUID.randomUUID().toString())
                .bodyValue(Map.of("user_id", username))
                .exchangeToMono(clientResponse -> handleResponse(clientResponse, username))
                .onErrorResume(this::onErrorResume);
    }

    private Mono<UserDetails> handleResponse(final ClientResponse clientResponse, final String username) {
        if (clientResponse.statusCode().isError()) {
            return handleErrorResponse(clientResponse, username);
        }

        return clientResponse.bodyToMono(parameterizedTypeReference)
                .flatMap(response -> processResponse(response, username));
    }

    private Mono<UserDetails> handleErrorResponse(final ClientResponse clientResponse, final String username) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(body -> {
                    if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new UsernameNotFoundException("User not found with id: " + username));
                    }

                    log.error("Error response received from auth service: {}", body);
                    return Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Non 200 response received from auth service"));
                });
    }

    private Mono<UserDetails> processResponse(final Map<String, Object> response, final String username) {
        if (response == null || response.isEmpty()) {
            return Mono.error(new IllegalStateException("Invalid response received from auth service"));
        }

        final boolean success = Boolean.parseBoolean(String.valueOf(response.get("success")));
        if (success) {
            return Mono.just(new User(username, "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        } else {
            return Mono.error(new UsernameNotFoundException("No user found with id: %s".formatted(username)));
        }
    }

    private Mono<UserDetails> onErrorResume(final Throwable e) {
        if (e instanceof UsernameNotFoundException) {
            return Mono.error(e);
        }
        return Mono.error(new UsernameNotFoundException("Failed to check admin user for userId: %s".formatted(e.getMessage())));
    }

}
