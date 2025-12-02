package com.example.stranglerdemo.client;

import com.example.stranglerdemo.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Client to interact with the legacy system API (jsonplaceholder)
 * Part of the Strangler Pattern - this wraps the old system while we build the new one
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LegacyApiClient {

    private final WebClient legacyApiWebClient;

    /**
     * Fetch all users from the legacy system
     */
    public List<User> getAllUsers() {
        log.info("Fetching all users from legacy system");
        return legacyApiWebClient
                .get()
                .uri("/users")
                .retrieve()
                .bodyToFlux(User.class)
                .collectList()
                .block();
    }

    /**
     * Fetch a single user by ID from the legacy system
     */
    public User getUserById(Long id) {
        log.info("Fetching user with id {} from legacy system", id);
        return legacyApiWebClient
                .get()
                .uri("/users/{id}", id)
                .retrieve()
                .bodyToMono(User.class)
                .block();
    }
}
