package com.bookbuddy.discovery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the Open Library source API.
 *
 * @param baseUrl       base URL of the Open Library API
 * @param connectTimeout connection timeout in milliseconds
 * @param readTimeout    read timeout in milliseconds
 * @param maxResults     maximum number of results to ingest per search
 */
@ConfigurationProperties(prefix = "openlibrary")
public record OpenLibraryProperties(
        String baseUrl,
        int connectTimeout,
        int readTimeout,
        int maxResults
) {
    public OpenLibraryProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://openlibrary.org";
        }
        if (connectTimeout <= 0) {
            connectTimeout = 5000;
        }
        if (readTimeout <= 0) {
            readTimeout = 10000;
        }
        if (maxResults <= 0) {
            maxResults = 20;
        }
    }
}
