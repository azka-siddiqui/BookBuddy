package com.bookbuddy.discovery.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Builds the {@link RestClient} used to call the Open Library API, with sensible
 * connect/read timeouts and a descriptive User-Agent (Open Library asks callers to
 * identify themselves).
 */
@Configuration
@EnableConfigurationProperties(OpenLibraryProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient openLibraryRestClient(OpenLibraryProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.connectTimeout()));
        factory.setReadTimeout(Duration.ofMillis(properties.readTimeout()));

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .defaultHeader("User-Agent", "BookBuddy/1.0 (discovery-service; contact: demo@bookbuddy.app)")
                .build();
    }
}
