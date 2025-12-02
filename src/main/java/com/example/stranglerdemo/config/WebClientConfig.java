package com.example.stranglerdemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient to consume the legacy system API
 */
@Configuration
public class WebClientConfig {

    @Value("${legacy.api.base-url}")
    private String legacyApiBaseUrl;

    @Bean
    public WebClient legacyApiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(legacyApiBaseUrl)
                .build();
    }
}
