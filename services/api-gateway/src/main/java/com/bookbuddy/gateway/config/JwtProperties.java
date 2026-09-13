package com.bookbuddy.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration for the gateway. The secret must match the one used by
 * social-service to sign tokens.
 *
 * @param secret Base64-encoded HS256 signing secret
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret) {
}
