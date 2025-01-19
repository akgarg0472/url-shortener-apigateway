package com.akgarg.us.apigw.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@Profile("dev")
public class InMemoryUserDetailsService implements ReactiveUserDetailsService {

    @Override
    public Mono<UserDetails> findByUsername(final String username) {
        return switch (username) {
            case "admin" -> Mono.just(new User(username, "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
            case "user" -> Mono.just(new User(username, "", List.of(new SimpleGrantedAuthority("ROLE_USER"))));
            default -> Mono.error(new UsernameNotFoundException("No user found with id: %s".formatted(username)));
        };
    }

}
