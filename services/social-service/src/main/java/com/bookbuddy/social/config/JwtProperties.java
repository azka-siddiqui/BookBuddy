package com.bookbuddy.social.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration.
 *
 * @param secret       Base64-encoded signing secret (must be at least 256 bits for HS256)
 * @param expirationMs token lifetime in milliseconds
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long expirationMs) {

    public JwtProperties {
        if (expirationMs <= 0) {
            expirationMs = 86_400_000L; // 24h
        }
    }
}
